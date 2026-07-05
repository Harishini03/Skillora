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
if (!token) {
    window.location.href = "/login.html";
}

const authHeaders = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`
};

const renderMetrics = async () => {
    const response = await fetch(`${apiBase}/api/staff/dashboard`, {headers: authHeaders});
    const data = await response.json();
    const metrics = document.getElementById("staffMetrics");
    metrics.innerHTML = `
        <div class="card"><h3>Total Students</h3><p>${data.totalStudents}</p></div>
        <div class="card"><h3>Eligible Students</h3><p>${data.eligibleStudents}</p></div>
        <div class="card"><h3>Placement Rate</h3><p>${data.placementRate.toFixed(2)}%</p></div>
        <div class="card"><h3>Avg CGPA</h3><p>${data.averageCgpa.toFixed(2)}</p></div>
    `;

    const deptTable = document.getElementById("departmentTable");
    deptTable.innerHTML = "<tr><th>Department</th><th>Eligibility %</th><th>Placement %</th><th>Avg Final Score</th></tr>";
    data.departmentSummaries.forEach(row => {
        deptTable.innerHTML += `<tr><td>${row.department}</td><td>${row.eligibilityPercentage.toFixed(2)}</td><td>${row.placementPercentage.toFixed(2)}</td><td>${row.averageFinalScore.toFixed(2)}</td></tr>`;
    });

    const skillTable = document.getElementById("skillTable");
    skillTable.innerHTML = "<tr><th>Skill</th><th>Student Count</th></tr>";
    data.skillGaps.forEach(row => {
        skillTable.innerHTML += `<tr><td>${row.skill}</td><td>${row.studentCount}</td></tr>`;
    });
};

const renderStudents = async () => {
    const response = await fetch(`${apiBase}/api/staff/students`, {headers: authHeaders});
    const data = await response.json();
    const table = document.getElementById("studentTable");
    table.innerHTML = "<tr><th>Name</th><th>Dept</th><th>Final Score</th><th>Status</th><th>Placement Ready</th></tr>";
    data.forEach(row => {
        table.innerHTML += `<tr><td>${row.name}</td><td>${row.department}</td><td>${row.finalScore || 0}</td><td>${row.placementStatus || "PENDING"}</td><td>${row.placementReady}</td></tr>`;
    });
};

renderMetrics();
renderStudents();
