const resolveApiBase = () => {
    if (window.location.protocol === "file:") {
        return "http://localhost:8080";
    }
    if ((window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1")
        && window.location.port !== "8080") {
        return "http://localhost:8080";
    }
    return "";
};

const apiBase = resolveApiBase();
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const LOGIN_DRAFT_KEY = "pi_login_draft_v1";
const SIGNUP_DRAFT_KEY = "pi_signup_draft_v1";
const ALLOWED_ROLES = new Set(["STUDENT", "STAFF", "RECRUITER"]);

const normalizeRole = (role) => ALLOWED_ROLES.has(role) ? role : "STUDENT";

const readDraft = (key, fallback) => {
    try {
        const raw = localStorage.getItem(key);
        if (!raw) return fallback;
        return {...fallback, ...JSON.parse(raw)};
    } catch (e) {
        return fallback;
    }
};

const storeAuth = (data) => {
    localStorage.setItem("token", data.token || "");
    localStorage.setItem("role", data.role || "");
    localStorage.setItem("studentId", data.studentId || "");
};

const redirectAfterLogin = (role) => {
    const normalizedRole = normalizeRole(role);
    if (normalizedRole === "STUDENT") {
        window.location.href = "/student-dashboard.html";
    } else {
        window.location.href = "/staff-dashboard.html";
    }
};

const redirectIfAuthenticated = () => {
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");
    if (!token || !role) {
        return;
    }
    if (window.location.pathname === "/login.html"
        || window.location.pathname === "/signup.html"
        || window.location.pathname === "/"
        || window.location.pathname.endsWith("/index.html")) {
        redirectAfterLogin(role);
    }
};

const readErrorMessage = async (response, fallback) => {
    try {
        const error = await response.json();
        if (error && error.message) {
            return error.message;
        }
    } catch (e) {
        // ignore JSON parsing errors
    }
    return fallback;
};

const isValidEmail = (email) => EMAIL_REGEX.test((email || "").trim());

const validateStrongPassword = (password) => {
    if (!password || password.length < 8) {
        return "Password must be at least 8 characters.";
    }
    const hasUpper = /[A-Z]/.test(password);
    const hasLower = /[a-z]/.test(password);
    const hasDigit = /[0-9]/.test(password);
    const hasSpecial = /[^A-Za-z0-9\s]/.test(password);
    if (!(hasUpper && hasLower && hasDigit && hasSpecial)) {
        return "Password must include uppercase, lowercase, number, and special character.";
    }
    return "";
};

const decodeJwtPayload = (idToken) => {
    if (!idToken || idToken.split(".").length < 2) {
        return {};
    }
    try {
        const payload = idToken.split(".")[1]
            .replace(/-/g, "+")
            .replace(/_/g, "/");
        const decoded = atob(payload.padEnd(Math.ceil(payload.length / 4) * 4, "="));
        return JSON.parse(decoded);
    } catch (e) {
        return {};
    }
};

const collectGoogleOnboarding = () => {
    const roleInput = normalizeRole((window.prompt("First-time Google sign-in: Enter role (STUDENT/STAFF/RECRUITER)", "STUDENT") || "").trim().toUpperCase());
    if (!ALLOWED_ROLES.has(roleInput)) {
        return null;
    }
    const departmentName = (window.prompt("Enter your department name", "Computer Science") || "").trim();
    if (!departmentName) {
        return null;
    }
    return {role: roleInput, departmentName};
};

const completeGoogleLogin = async (payload, statusElement) => {
    const response = await fetch(`${apiBase}/api/auth/google-login`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        const message = await readErrorMessage(response, "Google sign-in failed.");
        if (message.includes("Role is required") || message.includes("Department is required")) {
            const onboarding = collectGoogleOnboarding();
            if (!onboarding) {
                statusElement.textContent = "Google sign-in cancelled: role/department required.";
                return;
            }
            const retryResponse = await fetch(`${apiBase}/api/auth/google-login`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({...payload, ...onboarding})
            });
            if (!retryResponse.ok) {
                statusElement.textContent = await readErrorMessage(retryResponse, "Google sign-in failed.");
                return;
            }
            const retryData = await retryResponse.json();
            storeAuth(retryData);
            redirectAfterLogin(retryData.role);
            return;
        }
        statusElement.textContent = message;
        return;
    }

    const data = await response.json();
    storeAuth(data);
    redirectAfterLogin(data.role);
};

const runGoogleFallback = async (statusElement) => {
    const email = (window.prompt("Enter your Google email to continue") || "").trim();
    if (!isValidEmail(email)) {
        statusElement.textContent = "Enter a valid email address.";
        return;
    }
    const name = (window.prompt("Enter your name (optional)") || "").trim();
    await completeGoogleLogin({email, name}, statusElement);
};

const initGoogleLogin = () => {
    const googleBtn = document.getElementById("googleLoginBtn");
    if (!googleBtn) {
        return;
    }
    const status = document.getElementById("loginStatus");

    googleBtn.addEventListener("click", async () => {
        status.textContent = "Opening Google sign-in...";

        const clientConfigResponse = await fetch(`${apiBase}/api/auth/google-client-id`);
        if (!clientConfigResponse.ok) {
            status.textContent = "Unable to load Google configuration.";
            return;
        }
        const clientConfig = await clientConfigResponse.json();
        const clientId = (clientConfig.clientId || "").trim();

        if (!window.google || !window.google.accounts || !window.google.accounts.id || !clientId) {
            await runGoogleFallback(status);
            return;
        }

        const credentialResponse = await new Promise((resolve) => {
            let settled = false;
            const done = (value) => {
                if (!settled) {
                    settled = true;
                    resolve(value);
                }
            };

            window.google.accounts.id.initialize({
                client_id: clientId,
                ux_mode: "popup",
                callback: (response) => done(response)
            });

            window.google.accounts.id.prompt((notification) => {
                if (notification.isNotDisplayed() || notification.isSkippedMoment()) {
                    done(null);
                }
            });

            setTimeout(() => done(null), 7000);
        });

        if (!credentialResponse || !credentialResponse.credential) {
            await runGoogleFallback(status);
            return;
        }

        const payload = decodeJwtPayload(credentialResponse.credential);
        await completeGoogleLogin(
            {
                idToken: credentialResponse.credential,
                email: payload.email || "",
                name: payload.name || ""
            },
            status
        );
    });
};

const loginBtn = document.getElementById("loginBtn");
if (loginBtn) {
    const loginDraft = readDraft(LOGIN_DRAFT_KEY, {usernameOrEmail: ""});
    const usernameField = document.getElementById("login-username") || document.getElementById("username");
    if (usernameField && loginDraft.usernameOrEmail) {
        usernameField.value = loginDraft.usernameOrEmail;
    }

    loginBtn.addEventListener("click", async () => {
        const passwordField = document.getElementById("login-password") || document.getElementById("password");
        const status = document.getElementById("loginStatus");
        const usernameOrEmail = usernameField ? usernameField.value.trim() : "";
        const password = passwordField ? passwordField.value : "";
        localStorage.setItem(LOGIN_DRAFT_KEY, JSON.stringify({usernameOrEmail}));

        if (!usernameOrEmail || !password) {
            status.textContent = "Username/email and password are required.";
            return;
        }
        if (usernameOrEmail.includes("@") && !isValidEmail(usernameOrEmail)) {
            status.textContent = "Enter a valid email address.";
            return;
        }

        const response = await fetch(`${apiBase}/api/auth/login`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({usernameOrEmail, password})
        });

        if (!response.ok) {
            status.textContent = await readErrorMessage(response, "Login failed.");
            return;
        }
        const data = await response.json();
        storeAuth(data);
        localStorage.removeItem(LOGIN_DRAFT_KEY);
        redirectAfterLogin(data.role);
    });
}

const signupBtn = document.getElementById("signupBtn");
if (signupBtn) {
    const roleSelect = document.getElementById("signup-role") || document.getElementById("role");
    const studentFields = document.getElementById("signup-studentFields") || document.getElementById("studentFields");
    const signupDraft = readDraft(SIGNUP_DRAFT_KEY, {
        role: "STUDENT",
        username: "",
        email: "",
        name: "",
        departmentName: "",
        cgpa: "",
        level: "Beginner"
    });
    const usernameField = document.getElementById("signup-username") || document.getElementById("username");
    const emailField = document.getElementById("signup-email") || document.getElementById("email");
    const nameField = document.getElementById("signup-name") || document.getElementById("name");
    const departmentField = document.getElementById("signup-departmentName") || document.getElementById("departmentName");
    const cgpaField = document.getElementById("signup-cgpa") || document.getElementById("cgpa");
    const levelField = document.getElementById("signup-level") || document.getElementById("level");

    if (roleSelect) roleSelect.value = normalizeRole(signupDraft.role);
    if (usernameField && signupDraft.username) usernameField.value = signupDraft.username;
    if (emailField && signupDraft.email) emailField.value = signupDraft.email;
    if (nameField && signupDraft.name) nameField.value = signupDraft.name;
    if (departmentField && signupDraft.departmentName) departmentField.value = signupDraft.departmentName;
    if (cgpaField && signupDraft.cgpa !== "") cgpaField.value = signupDraft.cgpa;
    if (levelField && signupDraft.level) levelField.value = signupDraft.level;

    if (roleSelect && studentFields) {
        roleSelect.addEventListener("change", () => {
            studentFields.style.display = roleSelect.value === "STUDENT" ? "block" : "none";
            localStorage.setItem(SIGNUP_DRAFT_KEY, JSON.stringify({
                ...readDraft(SIGNUP_DRAFT_KEY, {}),
                role: normalizeRole(roleSelect.value)
            }));
        });
        studentFields.style.display = roleSelect.value === "STUDENT" ? "block" : "none";
    }

    signupBtn.addEventListener("click", async () => {
        const role = normalizeRole(roleSelect ? roleSelect.value : "STUDENT");
        const status = document.getElementById("signupStatus");
        const email = (emailField)?.value?.trim() || "";
        const password = (document.getElementById("signup-password") || document.getElementById("password"))?.value || "";

        localStorage.setItem(SIGNUP_DRAFT_KEY, JSON.stringify({
            role,
            username: usernameField?.value || "",
            email,
            name: nameField?.value || "",
            departmentName: departmentField?.value?.trim() || "",
            cgpa: cgpaField?.value || "",
            level: levelField?.value || "Beginner"
        }));

        if (!isValidEmail(email)) {
            status.textContent = "Enter a valid email address.";
            return;
        }
        const passwordMessage = validateStrongPassword(password);
        if (passwordMessage) {
            status.textContent = passwordMessage;
            return;
        }

        const payload = {
            username: usernameField?.value || "",
            email,
            password,
            role
        };

        if (role === "STUDENT") {
            payload.name = nameField?.value || "";
            payload.departmentName = departmentField?.value?.trim() || "";
            payload.cgpa = Number(cgpaField?.value || 0);
            payload.level = levelField?.value || "Beginner";
            payload.skillIds = [];
            if (!payload.departmentName) {
                status.textContent = "Please enter your department name.";
                return;
            }
        }

        const response = await fetch(`${apiBase}/api/auth/signup`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            status.textContent = await readErrorMessage(response, "Signup failed.");
            return;
        }
        const data = await response.json();
        storeAuth(data);
        localStorage.removeItem(SIGNUP_DRAFT_KEY);
        redirectAfterLogin(data.role);
    });
}

redirectIfAuthenticated();
initGoogleLogin();
