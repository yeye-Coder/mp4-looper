// MP4 Looper for Windows.
// A tiny launcher: mp4-looper.html is embedded inside this .exe. On start it is copied to
// %LOCALAPPDATA%\MP4Looper and opened in Microsoft Edge "app mode" (own window, no address bar),
// with its own browser profile so saved sentences are kept separately from normal browsing.
// Written for the C# 5 compiler that ships with Windows (.NET Framework 4.x).

using System;
using System.Diagnostics;
using System.IO;
using System.Reflection;
using System.Windows.Forms;
using Microsoft.Win32;

static class Program
{
    const string Title = "MP4 Looper";

    [STAThread]
    static void Main()
    {
        try
        {
            string dir = Path.Combine(
                Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "MP4Looper");
            Directory.CreateDirectory(dir);

            string htmlPath = Path.Combine(dir, "mp4-looper.html");
            byte[] html = ReadEmbeddedPage();
            if (!File.Exists(htmlPath) || !SameBytes(File.ReadAllBytes(htmlPath), html))
                File.WriteAllBytes(htmlPath, html);

            string browser = FindBrowser();
            if (browser == null)
            {
                MessageBox.Show("Microsoft Edge or Google Chrome is needed to run MP4 Looper, but neither was found.",
                    Title, MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            string url = new Uri(htmlPath).AbsoluteUri;
            string profile = Path.Combine(dir, "BrowserProfile");
            string args =
                "--app=\"" + url + "\"" +
                " --user-data-dir=\"" + profile + "\"" +
                " --window-size=440,990" +
                " --no-first-run --no-default-browser-check";

            ProcessStartInfo psi = new ProcessStartInfo(browser, args);
            psi.UseShellExecute = false;
            Process.Start(psi);
        }
        catch (Exception e)
        {
            MessageBox.Show("MP4 Looper could not start:\n\n" + e.Message,
                Title, MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }

    static byte[] ReadEmbeddedPage()
    {
        using (Stream s = Assembly.GetExecutingAssembly().GetManifestResourceStream("mp4-looper.html"))
        using (MemoryStream ms = new MemoryStream())
        {
            if (s == null) throw new Exception("The page is missing from the program.");
            s.CopyTo(ms);
            return ms.ToArray();
        }
    }

    static bool SameBytes(byte[] a, byte[] b)
    {
        if (a.Length != b.Length) return false;
        for (int i = 0; i < a.Length; i++) if (a[i] != b[i]) return false;
        return true;
    }

    // Prefer Edge (part of Windows 10/11); fall back to Chrome.
    static string FindBrowser()
    {
        string[] exes = { "msedge.exe", "chrome.exe" };
        foreach (string exe in exes)
        {
            string p = FromAppPaths(Registry.CurrentUser, exe) ?? FromAppPaths(Registry.LocalMachine, exe);
            if (p != null) return p;
        }
        string pf86 = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86);
        string pf = Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles);
        string local = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
        string[] candidates = {
            Path.Combine(pf86, @"Microsoft\Edge\Application\msedge.exe"),
            Path.Combine(pf, @"Microsoft\Edge\Application\msedge.exe"),
            Path.Combine(pf, @"Google\Chrome\Application\chrome.exe"),
            Path.Combine(pf86, @"Google\Chrome\Application\chrome.exe"),
            Path.Combine(local, @"Google\Chrome\Application\chrome.exe"),
        };
        foreach (string c in candidates) if (File.Exists(c)) return c;
        return null;
    }

    static string FromAppPaths(RegistryKey hive, string exe)
    {
        try
        {
            using (RegistryKey k = hive.OpenSubKey(@"SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\" + exe))
            {
                string p = k == null ? null : k.GetValue(null) as string;
                if (p != null) p = p.Trim('"');
                return (p != null && File.Exists(p)) ? p : null;
            }
        }
        catch { return null; }
    }
}
