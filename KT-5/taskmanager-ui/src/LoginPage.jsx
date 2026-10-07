import { useState } from "react";

const inputStyle = { padding: 8, borderRadius: 10, border: "1px solid #ccc" };

export default function LoginPage({ apiBase, onLogin }) {
    const [mode, setMode] = useState("login");
    const [form, setForm] = useState({ username: "", email: "", password: "" });
    const [error, setError] = useState(null);
    const [busy, setBusy] = useState(false);

    const isRegister = mode === "register";

    async function submit(e) {
        e.preventDefault();
        setError(null);
        setBusy(true);
        try {
            if (isRegister) {
                const res = await fetch(`${apiBase}/auth/register`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json", "X-Requested-With": "XMLHttpRequest" },
                    body: JSON.stringify(form),
                });
                if (!res.ok) {
                    const body = await res.json().catch(() => null);
                    throw { status: res.status, body, message: body?.message || `Request failed: ${res.status}` };
                }
            }
            await onLogin(form.username, form.password);
        } catch (err) {
            setError(err);
        } finally {
            setBusy(false);
        }
    }

    function switchMode() {
        setMode(isRegister ? "login" : "register");
        setError(null);
    }

    const validationErrors = error?.body?.errors;

    return (
        <div style={{ fontFamily: "system-ui, sans-serif", padding: 18, maxWidth: 380, margin: "60px auto" }}>
            <h2 style={{ margin: "0 0 16px" }}>Task Manager</h2>
            <div style={{ border: "1px solid #ddd", borderRadius: 12, padding: 14 }}>
                <strong>{isRegister ? "Registration" : "Sign in"}</strong>
                <form onSubmit={submit} style={{ display: "grid", gap: 10, marginTop: 10 }}>
                    <label style={{ display: "grid", gap: 6 }}>
                        <span>Username</span>
                        <input
                            value={form.username}
                            onChange={(e) => setForm((p) => ({ ...p, username: e.target.value }))}
                            autoComplete="username"
                            style={inputStyle}
                        />
                    </label>

                    {isRegister && (
                        <label style={{ display: "grid", gap: 6 }}>
                            <span>Email</span>
                            <input
                                type="email"
                                value={form.email}
                                onChange={(e) => setForm((p) => ({ ...p, email: e.target.value }))}
                                autoComplete="email"
                                style={inputStyle}
                            />
                        </label>
                    )}

                    <label style={{ display: "grid", gap: 6 }}>
                        <span>Password</span>
                        <input
                            type="password"
                            value={form.password}
                            onChange={(e) => setForm((p) => ({ ...p, password: e.target.value }))}
                            autoComplete={isRegister ? "new-password" : "current-password"}
                            style={inputStyle}
                        />
                    </label>

                    <button
                        type="submit"
                        disabled={busy}
                        style={{ padding: "10px 12px", borderRadius: 12, border: "1px solid #333", background: "#111", color: "white", cursor: "pointer" }}
                    >
                        {isRegister ? "Create account" : "Sign in"}
                    </button>
                </form>

                <button type="button" onClick={switchMode} style={{ marginTop: 10, padding: "6px 10px" }}>
                    {isRegister ? "I already have an account" : "Create a new account"}
                </button>

                {error && (
                    <div style={{ marginTop: 10, padding: 10, borderRadius: 12, background: "#fff1f1", border: "1px solid #f0b4b4" }}>
                        <div>{error.message || "Request failed"}</div>
                        {Array.isArray(validationErrors) && validationErrors.length > 0 && (
                            <ul style={{ margin: "6px 0 0", paddingLeft: 18 }}>
                                {validationErrors.map((ve, idx) => (
                                    <li key={idx}>
                                        <code>{ve.field}</code>: {ve.message}
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}
