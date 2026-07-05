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

const chartState = {
    placementData: [],
    difficultyData: [],
    topicData: [],
    departmentData: []
};

const defaultChartRows = (labelKey) => [
    {[labelKey]: "No Data", value: 0}
];

const normalizeRows = (rows, labelKey, valueKey) => {
    if (!Array.isArray(rows) || rows.length === 0) {
        return defaultChartRows(labelKey);
    }
    return rows.map((item) => ({
        [labelKey]: item[labelKey],
        value: Number(item[valueKey]) || 0
    }));
};

const getDepartmentValues = (rows, metric) => {
    return rows.map((item) => {
        if (metric === "aptitude") {
            return Number(item.aptitudeAvg) || 0;
        }
        if (metric === "dsa") {
            return Number(item.dsaAvg) || 0;
        }
        if (metric === "mock") {
            return Number(item.mockAvg) || 0;
        }
        return ((Number(item.aptitudeAvg) || 0) + (Number(item.dsaAvg) || 0) + (Number(item.mockAvg) || 0)) / 3;
    });
};

const drawAxis = (ctx, width, height) => {
    ctx.strokeStyle = "rgba(13, 20, 37, 0.18)";
    ctx.lineWidth = 1;
    ctx.beginPath();
    ctx.moveTo(36, 12);
    ctx.lineTo(36, height - 26);
    ctx.lineTo(width - 12, height - 26);
    ctx.stroke();
};

const drawValueGuides = (ctx, width, height, max) => {
    const steps = 4;
    for (let i = 1; i <= steps; i += 1) {
        const y = 12 + ((height - 38) * i) / steps;
        const value = Math.round(max * (1 - i / steps));
        ctx.strokeStyle = "rgba(13, 20, 37, 0.08)";
        ctx.beginPath();
        ctx.moveTo(36, y);
        ctx.lineTo(width - 12, y);
        ctx.stroke();
        ctx.fillStyle = "#58667c";
        ctx.font = "11px Sora";
        ctx.fillText(String(value), 8, y + 4);
    }
};

const drawRoundedBar = (ctx, x, y, width, height, radius) => {
    if (typeof ctx.roundRect === "function") {
        ctx.beginPath();
        ctx.roundRect(x, y, width, height, radius);
        ctx.fill();
        return;
    }
    ctx.beginPath();
    ctx.rect(x, y, width, height);
    ctx.fill();
};

const drawBarChart = (canvasId, labels, values, startColor, endColor) => {
    const canvas = document.getElementById(canvasId);
    if (!canvas) {
        return;
    }
    const ctx = canvas.getContext("2d");
    const width = canvas.width = Math.max(canvas.clientWidth, 260);
    const height = canvas.height = Math.max(canvas.clientHeight, 220);
    ctx.clearRect(0, 0, width, height);

    const safeValues = values.length ? values : [0];
    const safeLabels = labels.length ? labels : ["No Data"];
    const max = Math.max(...safeValues, 1);
    const usableWidth = width - 56;
    const barWidth = Math.max((usableWidth / safeLabels.length) * 0.56, 18);

    drawValueGuides(ctx, width, height, max);
    drawAxis(ctx, width, height);

    const gradient = ctx.createLinearGradient(0, 20, width, height - 24);
    gradient.addColorStop(0, startColor);
    gradient.addColorStop(1, endColor);

    safeLabels.forEach((label, index) => {
        const value = safeValues[index] || 0;
        const barHeight = (value / max) * (height - 52);
        const step = usableWidth / safeLabels.length;
        const x = 40 + (step * index + step / 2 - barWidth / 2);
        const y = height - barHeight - 26;

        ctx.fillStyle = gradient;
        drawRoundedBar(ctx, x, y, barWidth, barHeight, 8);

        ctx.fillStyle = "#111f33";
        ctx.font = "11px Sora";
        ctx.fillText(String(Math.round(value)), x + 1, y - 6);
        ctx.fillStyle = "#34455d";
        ctx.fillText(label.length > 11 ? `${label.slice(0, 10)}.` : label, x, height - 8);
    });
};

const drawLineChart = (canvasId, labels, values, lineColor, fillColor) => {
    const canvas = document.getElementById(canvasId);
    if (!canvas) {
        return;
    }
    const ctx = canvas.getContext("2d");
    const width = canvas.width = Math.max(canvas.clientWidth, 260);
    const height = canvas.height = Math.max(canvas.clientHeight, 220);
    ctx.clearRect(0, 0, width, height);

    const safeValues = values.length ? values : [0];
    const safeLabels = labels.length ? labels : ["No Data"];
    const max = Math.max(...safeValues, 1);
    const step = (width - 64) / Math.max(safeLabels.length - 1, 1);

    drawValueGuides(ctx, width, height, max);
    drawAxis(ctx, width, height);

    ctx.beginPath();
    safeValues.forEach((value, index) => {
        const x = 40 + step * index;
        const y = height - 26 - (value / max) * (height - 52);
        if (index === 0) {
            ctx.moveTo(x, y);
        } else {
            ctx.lineTo(x, y);
        }
    });
    ctx.strokeStyle = lineColor;
    ctx.lineWidth = 2.5;
    ctx.stroke();

    ctx.lineTo(40 + step * (safeValues.length - 1), height - 26);
    ctx.lineTo(40, height - 26);
    ctx.closePath();
    ctx.fillStyle = fillColor;
    ctx.fill();

    safeValues.forEach((value, index) => {
        const x = 40 + step * index;
        const y = height - 26 - (value / max) * (height - 52);
        ctx.fillStyle = lineColor;
        ctx.beginPath();
        ctx.arc(x, y, 3.8, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = "#34455d";
        ctx.font = "11px Sora";
        ctx.fillText(safeLabels[index].length > 11 ? `${safeLabels[index].slice(0, 10)}.` : safeLabels[index], x - 12, height - 8);
    });
};

const drawSeries = (canvasId, labels, values, colors, mode) => {
    if (mode === "line") {
        drawLineChart(canvasId, labels, values, colors.line, colors.fill);
        return;
    }
    drawBarChart(canvasId, labels, values, colors.start, colors.end);
};

const average = (values) => {
    if (!values.length) {
        return 0;
    }
    return values.reduce((sum, value) => sum + value, 0) / values.length;
};

const setText = (id, text) => {
    const element = document.getElementById(id);
    if (element) {
        element.textContent = text;
    }
};

const renderInsights = () => {
    const insightList = document.getElementById("insightList");
    const departmentLeaderboard = document.getElementById("departmentLeaderboard");
    if (!insightList || !departmentLeaderboard) {
        return;
    }
    insightList.innerHTML = "";
    departmentLeaderboard.innerHTML = "";

    const placementAvg = average(chartState.placementData.map((item) => item.value));
    const readinessAvg = average(getDepartmentValues(chartState.departmentData, "composite"));
    const weakestTopic = chartState.topicData.reduce((lowest, current) => current.value < lowest.value ? current : lowest, {topic: "N/A", value: 0});
    const topDepartment = chartState.departmentData
        .map((item) => ({
            ...item,
            composite: ((Number(item.aptitudeAvg) || 0) + (Number(item.dsaAvg) || 0) + (Number(item.mockAvg) || 0)) / 3
        }))
        .reduce((best, current) => current.composite > best.composite ? current : best, {department: "N/A", composite: 0});

    setText("kpiPlacementIndex", `${Math.round(placementAvg)}%`);
    setText("kpiReadiness", `${Math.round(readinessAvg)}%`);
    setText("kpiTopDepartment", topDepartment.department);
    setText("kpiRiskTopic", weakestTopic.topic || "N/A");
    setText("hiringNarrative", `${topDepartment.department} currently leads with ${Math.round(topDepartment.composite)}% composite score, while ${weakestTopic.topic} needs immediate reinforcement.`);

    const insights = [
        `Placement performance index is at ${Math.round(placementAvg)}%. Prioritize high-readiness pools for next drive.`,
        `Overall campus readiness is ${Math.round(readinessAvg)}%, indicating ${readinessAvg >= 70 ? "strong" : "moderate"} hiring momentum.`,
        `${weakestTopic.topic} is the weakest topic at ${Math.round(weakestTopic.value)}% accuracy. Add targeted workshops before screenings.`,
        `${topDepartment.department} is the top-performing department with ${Math.round(topDepartment.composite)}% composite output.`
    ];

    insights.forEach((insight) => {
        const li = document.createElement("li");
        li.textContent = insight;
        insightList.appendChild(li);
    });

    chartState.departmentData
        .map((item) => ({
            department: item.department,
            aptitudeAvg: Number(item.aptitudeAvg) || 0,
            dsaAvg: Number(item.dsaAvg) || 0,
            mockAvg: Number(item.mockAvg) || 0,
            composite: ((Number(item.aptitudeAvg) || 0) + (Number(item.dsaAvg) || 0) + (Number(item.mockAvg) || 0)) / 3
        }))
        .sort((a, b) => b.composite - a.composite)
        .forEach((item) => {
            const tr = document.createElement("tr");
            tr.innerHTML = `
                <td>${item.department}</td>
                <td>${Math.round(item.composite)}%</td>
                <td>${Math.round(item.aptitudeAvg)}%</td>
                <td>${Math.round(item.dsaAvg)}%</td>
                <td>${Math.round(item.mockAvg)}%</td>
            `;
            departmentLeaderboard.appendChild(tr);
        });
};

const renderCharts = () => {
    const mode = document.getElementById("chartViewMode")?.value || "bar";
    const departmentMetric = document.getElementById("departmentMetric")?.value || "composite";

    drawSeries(
        "placementChart",
        chartState.placementData.map((item) => item.label),
        chartState.placementData.map((item) => item.value),
        {start: "#ff9a3f", end: "#ff6f3e", line: "#e5612c", fill: "rgba(245, 123, 52, 0.18)"},
        mode
    );

    drawSeries(
        "difficultyChart",
        chartState.difficultyData.map((item) => item.difficulty),
        chartState.difficultyData.map((item) => item.value),
        {start: "#35d2b5", end: "#2cb5f0", line: "#22a68f", fill: "rgba(45, 195, 167, 0.2)"},
        mode
    );

    drawSeries(
        "topicChart",
        chartState.topicData.map((item) => item.topic),
        chartState.topicData.map((item) => item.value),
        {start: "#ffcf55", end: "#f5a623", line: "#d58a16", fill: "rgba(245, 166, 35, 0.22)"},
        mode
    );

    drawSeries(
        "departmentChart",
        chartState.departmentData.map((item) => item.department),
        getDepartmentValues(chartState.departmentData, departmentMetric),
        {start: "#5f7dff", end: "#5b54f7", line: "#465fde", fill: "rgba(91, 107, 255, 0.2)"},
        mode
    );
};

const bindControls = () => {
    const modeControl = document.getElementById("chartViewMode");
    const metricControl = document.getElementById("departmentMetric");
    modeControl?.addEventListener("change", renderCharts);
    metricControl?.addEventListener("change", renderCharts);
    window.addEventListener("resize", renderCharts);
};

const loadAnalytics = async () => {
    try {
        const response = await fetch(`${apiBase}/api/staff/analytics`, {headers: authHeaders});
        if (!response.ok) {
            throw new Error("Unable to load analytics.");
        }
        const data = await response.json();

        chartState.placementData = normalizeRows(data.testScoreVsPlacement, "label", "averageFinalScore");
        chartState.difficultyData = normalizeRows(data.difficultyPerformance, "difficulty", "accuracy");
        chartState.topicData = normalizeRows(data.weakTopics, "topic", "accuracy");
        chartState.departmentData = Array.isArray(data.departmentAverageScores) && data.departmentAverageScores.length
            ? data.departmentAverageScores
            : [{department: "No Data", aptitudeAvg: 0, dsaAvg: 0, mockAvg: 0}];

        renderInsights();
        renderCharts();
    } catch (error) {
        setText("hiringNarrative", "Analytics unavailable right now. Please retry.");
        setText("kpiPlacementIndex", "N/A");
        setText("kpiReadiness", "N/A");
        setText("kpiTopDepartment", "N/A");
        setText("kpiRiskTopic", "N/A");
    }
};

bindControls();
loadAnalytics();
