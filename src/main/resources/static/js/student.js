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
const token = localStorage.getItem("token");
const studentId = localStorage.getItem("studentId");
if (!token || !studentId) {
    window.location.href = "/login.html";
}

const authHeaders = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`
};

const renderSummary = async () => {
    const response = await fetch(`${apiBase}/api/student/${studentId}/dashboard`, {headers: authHeaders});
    const data = await response.json();
    const container = document.getElementById("studentSummary");
    container.innerHTML = `
        <div class="card"><h3>${data.name}</h3><p>${data.department}</p><p class="pill">${data.placementStatus || "PENDING"}</p></div>
        <div class="card"><h3>Scores</h3><p>Aptitude: ${data.aptitudeScore || 0}</p><p>DSA: ${data.dsaScore || 0}</p><p>Mock: ${data.mockTestScore || 0}</p></div>
        <div class="card"><h3>Final Score</h3><p>${data.finalScore || 0}</p><p>CGPA: ${data.cgpa}</p></div>
    `;
};

const renderHistory = async () => {
    const response = await fetch(`${apiBase}/api/student/${studentId}/history`, {headers: authHeaders});
    const data = await response.json();
    const table = document.getElementById("historyTable");
    table.innerHTML = "<tr><th>Date</th><th>Type</th><th>Score</th><th>Total</th></tr>";
    data.forEach(item => {
        table.innerHTML += `<tr><td>${item.testDate}</td><td>${item.testType}</td><td>${item.score}</td><td>${item.totalQuestions}</td></tr>`;
    });
};

const startTest = async (type) => {
    let url = `${apiBase}/api/student/${studentId}/tests/${type.toLowerCase()}`;
    if (type === "CODING") {
        const companyId = prompt("Enter company id (1-5) or leave blank for random:");
        if (companyId) {
            url += `?companyId=${companyId}`;
        }
    }
    const response = await fetch(url, {headers: authHeaders});
    const data = await response.json();
    renderTest(data);
};

const renderTest = (data) => {
    const area = document.getElementById("testArea");
    const questionsHtml = data.questions.map((q, index) => `
        <div style="margin-bottom:16px;">
            <strong>${index + 1}. ${q.questionText}</strong>
            <div><label><input type="radio" name="q${q.id}" value="A"> ${q.optionA}</label></div>
            <div><label><input type="radio" name="q${q.id}" value="B"> ${q.optionB}</label></div>
            <div><label><input type="radio" name="q${q.id}" value="C"> ${q.optionC}</label></div>
            <div><label><input type="radio" name="q${q.id}" value="D"> ${q.optionD}</label></div>
        </div>
    `).join("");

    area.innerHTML = `
        <h3>${data.testType} Test (Duration: ${data.durationMinutes} mins)</h3>
        ${questionsHtml}
        <button class="button" id="submitTestBtn">Submit Test</button>
    `;

    document.getElementById("submitTestBtn").addEventListener("click", () => submitTest(data));
};

const submitTest = async (data) => {
    const answers = {};
    data.questions.forEach(q => {
        const selected = document.querySelector(`input[name="q${q.id}"]:checked`);
        if (selected) {
            answers[q.id] = selected.value;
        }
    });

    const response = await fetch(`${apiBase}/api/student/${studentId}/tests/submit`, {
        method: "POST",
        headers: authHeaders,
        body: JSON.stringify({testType: data.testType, sessionId: data.sessionId, answers})
    });

    if (!response.ok) {
        alert("Submission failed or session expired.");
        return;
    }
    await renderSummary();
    await renderHistory();
    document.getElementById("testArea").innerHTML = "<p>Test submitted successfully.</p>";
};

renderSummary();
renderHistory();
