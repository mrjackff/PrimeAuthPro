using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace authproexample
{
    public partial class Form1 : Form
    {
        private string appId = "c379560000";
        private string secretKey = "8ad92d0a650a178cb642";
        private string version = "2.0";

        public Form1()
        {
            InitializeComponent();
        }

        private async void Form1_Load(object sender, EventArgs e)
        {
            InitResult initResult = await AuthHelper.Init(appId, secretKey, version);
            if (!initResult.Success)
            {
                this.Close();
            }
        }

        private async void button1_Click(object sender, EventArgs e)
        {
            string username = textBox1.Text.Trim();
            string password = textBox2.Text.Trim();

            if (string.IsNullOrEmpty(username) || string.IsNullOrEmpty(password))
            {
                MessageBox.Show("Please enter both username and password.");
                return;
            }

            LoginResult loginResult = await AuthHelper.Login(appId, secretKey, username, password, version);

            if (loginResult.Success)
            {
                if (loginResult.Package == "owner")
                {
                    MessageBox.Show($"Login: {loginResult.Message}");
                    Form2 dashboard = new Form2();
                    dashboard.Show();
                    this.Hide();
                }
                else
                {
                    MessageBox.Show($"Access denied.","Insufficient Permissions", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                }
            }
            else
            {
                MessageBox.Show("Login failed: " + loginResult.Message, "Login Error",
                              MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }
    }
}