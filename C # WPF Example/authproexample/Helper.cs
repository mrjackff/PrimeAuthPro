using System.Management;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text.Json;
using System.Text;
using System;
using System.Net.Http;
using System.Threading.Tasks;

public class LoginResult
{
    public bool Success { get; set; }
    public string Message { get; set; }
    public string Package { get; set; }
}

public class InitResult
{
    public bool Success { get; set; }
    public string ErrorMessage { get; set; }
}

public static class AuthHelper
{
    private const string VALIDATE_URL = "https://primeauthpro.com/api/validate.php";
    private static string _initToken = null;

    private const uint TOKEN_READ = 0x20008;
    private const int TokenUser = 1;

    [DllImport("advapi32.dll", SetLastError = true)]
    static extern bool OpenProcessToken(IntPtr ProcessHandle, uint DesiredAccess, out IntPtr TokenHandle);

    [DllImport("kernel32.dll")]
    static extern IntPtr GetCurrentProcess();

    [DllImport("advapi32.dll", SetLastError = true)]
    static extern bool GetTokenInformation(IntPtr TokenHandle, int TokenInformationClass, IntPtr TokenInformation, int TokenInformationLength, out int ReturnLength);

    [DllImport("advapi32.dll", SetLastError = true, CharSet = CharSet.Auto)]
    static extern bool ConvertSidToStringSid(IntPtr pSid, out IntPtr ptrSid);

    [DllImport("kernel32.dll")]
    static extern IntPtr LocalFree(IntPtr hMem);

    [StructLayout(LayoutKind.Sequential)]
    struct TOKEN_USER
    {
        public _SID_AND_ATTRIBUTES User;
    }

    [StructLayout(LayoutKind.Sequential)]
    struct _SID_AND_ATTRIBUTES
    {
        public IntPtr Sid;
        public int Attributes;
    }

    public static string GetWindowsSid()
    {
        IntPtr tokenHandle;
        if (!OpenProcessToken(GetCurrentProcess(), TOKEN_READ, out tokenHandle))
            return "";

        int tokenInfoLength = 0;
        GetTokenInformation(tokenHandle, TokenUser, IntPtr.Zero, 0, out tokenInfoLength);
        IntPtr tokenInfo = Marshal.AllocHGlobal(tokenInfoLength);

        if (!GetTokenInformation(tokenHandle, TokenUser, tokenInfo, tokenInfoLength, out tokenInfoLength))
        {
            Marshal.FreeHGlobal(tokenInfo);
            return "";
        }

        TOKEN_USER tokenUser = Marshal.PtrToStructure<TOKEN_USER>(tokenInfo);
        IntPtr pStringSid;
        if (!ConvertSidToStringSid(tokenUser.User.Sid, out pStringSid))
        {
            Marshal.FreeHGlobal(tokenInfo);
            return "";
        }

        string sidString = Marshal.PtrToStringAuto(pStringSid);
        LocalFree(pStringSid);
        Marshal.FreeHGlobal(tokenInfo);
        return sidString;
    }

    public static string GetProcessorId()
    {
        try
        {
            var searcher = new ManagementObjectSearcher("select ProcessorId from Win32_Processor");
            foreach (ManagementObject obj in searcher.Get())
            {
                return obj["ProcessorId"]?.ToString()?.Trim() ?? "";
            }
        }
        catch { }
        return "";
    }

    static string Base64UrlEncode(byte[] input) =>
        Convert.ToBase64String(input).TrimEnd('=').Replace('+', '-').Replace('/', '_');

    static string Base64UrlEncode(string input) => Base64UrlEncode(Encoding.UTF8.GetBytes(input));

    static string HmacSha256(string data, string key)
    {
        using (var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(key)))
        {
            byte[] hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(data));
            return Base64UrlEncode(hash);
        }
    }

    public static string GenerateJwt(object payload, string secret)
    {
        var header = new { alg = "HS256", typ = "JWT" };
        string headerJson = JsonSerializer.Serialize(header);
        string payloadJson = JsonSerializer.Serialize(payload);

        string headerEncoded = Base64UrlEncode(headerJson);
        string payloadEncoded = Base64UrlEncode(payloadJson);
        string signature = HmacSha256(headerEncoded + "." + payloadEncoded, secret);

        return headerEncoded + "." + payloadEncoded + "." + signature;
    }

    public static async Task<InitResult> Init(string appId, string secretKey, string version)
    {
        var payload = new { app_id = appId, secret_key = secretKey, version = version };
        string jwtToken = GenerateJwt(payload, "SuperSecretKey987654321");

        var client = new HttpClient();
        try
        {
            client.Timeout = TimeSpan.FromSeconds(30);
            client.DefaultRequestHeaders.Add("Accept", "application/json");

            var response = await client.GetAsync(VALIDATE_URL + "?token=" + Uri.EscapeDataString(jwtToken) + "&mode=init");
            string responseContent = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode || responseContent.TrimStart().StartsWith("<"))
            {
                string errorMsg = "Server error or invalid format";
                Console.WriteLine($"Init Error: {errorMsg}");
                return new InitResult { Success = false, ErrorMessage = errorMsg };
            }

            using (var jsonDoc = JsonDocument.Parse(responseContent))
            {
                var root = jsonDoc.RootElement;
                string status = root.GetProperty("status").GetString();

                if (status != "success")
                {
                    string message = root.GetProperty("message").GetString();
                    Console.WriteLine($"Init Error: {message}");
                    return new InitResult { Success = false, ErrorMessage = message };
                }

                if (root.TryGetProperty("init_token", out var initTokenElement))
                {
                    _initToken = initTokenElement.GetString();
                    return new InitResult { Success = true, ErrorMessage = null };
                }
                else
                {
                    string errorMsg = "Initialization token not found";
                    Console.WriteLine($"Init Error: {errorMsg}");
                    return new InitResult { Success = false, ErrorMessage = errorMsg };

                }
            }
        }
        catch (Exception ex)
        {
            string errorMsg = "Init failed: " + ex.Message;
            Console.WriteLine($"Init Error: {errorMsg}");
            return new InitResult { Success = false, ErrorMessage = errorMsg };
        }
        finally
        {
            client.Dispose();
        }
    }

    public static async Task<LoginResult> Login(string appId, string secretKey, string username, string password, string version)
    {
        if (string.IsNullOrEmpty(_initToken))
            return new LoginResult { Success = false, Message = "App not initialized" };

        string hwid = GetWindowsSid();
        string processorId = GetProcessorId();

        var payload = new
        {
            app_id = appId,
            secret_key = secretKey,
            username = username,
            password = password,
            version = version,
            hwid = hwid,
            processor_id = processorId,
            init_token = _initToken
        };

        string jwtToken = GenerateJwt(payload, "SuperSecretKey987654321");

        var client = new HttpClient();
        try
        {
            client.Timeout = TimeSpan.FromSeconds(30);
            var response = await client.GetAsync(VALIDATE_URL + "?token=" + Uri.EscapeDataString(jwtToken));
            string responseContent = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode || responseContent.TrimStart().StartsWith("<"))
                return new LoginResult { Success = false, Message = "Server error or invalid format" };

            using (var jsonDoc = JsonDocument.Parse(responseContent))
            {
                var root = jsonDoc.RootElement;
                string message = root.GetProperty("message").GetString();
                string status = root.GetProperty("status").GetString();

                if (status == "success")
                {
                    string package = "";
                    if (root.TryGetProperty("user_package", out var userPackageElement) &&
                        userPackageElement.TryGetProperty("package", out var packageElement))
                    {
                        package = packageElement.GetString();
                    }

                    return new LoginResult
                    {
                        Success = true,
                        Message = message,
                        Package = package
                    };
                }
                else
                {
                    return new LoginResult
                    {
                        Success = false,
                        Message = message
                    };
                }
            }
        }
        catch (Exception ex)
        {
            return new LoginResult
            {
                Success = false,
                Message = "Login failed: " + ex.Message
            };
        }
        finally
        {
            client.Dispose();
        }
    }

    public static void ClearSession()
    {
        _initToken = null;
    }
}