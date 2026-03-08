let chart;

/*
async function uploadFile(){

const file=document.getElementById("fileInput").files[0];

const formData=new FormData();

formData.append("file",file);

await fetch("http://localhost:5000/upload",{

method:"POST",
body:formData

});

alert("File uploaded successfully");

}

*/



async function sendQuery() {

    const query = document.getElementById("queryInput").value;

    const response = await fetch("http://localhost:8000/generate-dashboard", {

        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify({
            question: query
        })

    });

    const result = await response.json();

    if (result.status !== "success") {
        alert("Error from API");
        return;
    }

    const apiData = result.data;

    /* convert API format → chart format */

    const data = apiData.table_data.map(row => ({
        name: row.life_insurer,
        value: row.percentage_value_at_risk
    }));

    /* update recommendation */

    document.getElementById("recommendedChart").innerText = apiData.chart_type;

    /* auto-select chart */

    const chartMap = {
        "Pie Chart": "pie",
        "Bar Chart": "bar",
        "Line Chart": "line"
    };

    const chartType = chartMap[apiData.chart_type] || "bar";

    document.getElementById("chartType").value = chartType;

    window.lastData = data;

    /* render */

    createTable(data);
    createChart(data);

    /* optional summary display */

    alert(apiData.summary);

}


function createTable(data) {

    const tableHead = document.querySelector("#resultTable thead");
    const tableBody = document.querySelector("#resultTable tbody");

    tableHead.innerHTML = "";
    tableBody.innerHTML = "";

    let headRow = "<tr><th>Life Insurer</th><th>Percentage Value At Risk</th></tr>";

    tableHead.innerHTML = headRow;

    data.forEach(row => {

        let tr = `<tr>
        <td>${row.name}</td>
        <td>${row.value.toFixed(2)}%</td>
        </tr>`;

        tableBody.innerHTML += tr;

    });

}

function createChart(data) {

    /* limit to top 10 for visualization */

    data = data
        .sort((a, b) => b.value - a.value)
        .slice(0, 10);

    const labels = data.map(d => d.name);
    const values = data.map(d => d.value);

    const chartType = document.getElementById("chartType").value;

    const ctx = document.getElementById("chartCanvas");

    if (chart) {
        chart.destroy();
    }

    chart = new Chart(ctx, {

        type: chartType,

        data: {
            labels: labels,
            datasets: [{
                label: "Value at Risk %",
                data: values,
                backgroundColor: [
                    "#2563eb", "#22c55e", "#f59e0b", "#ef4444",
                    "#8b5cf6", "#14b8a6", "#6366f1", "#e11d48",
                    "#10b981", "#f97316"
                ],
                borderWidth: 1
            }]
        },

        options: {
            responsive: true,
            maintainAspectRatio: false
        }

    });

}
function recommendChart(data) {

    const count = data.length;

    if (count <= 5) {
        return "pie";
    }

    if (count <= 10) {
        return "bar";
    }

    return "line";

}

document.getElementById("chartType").addEventListener("change", function () {

    if (window.lastData) {
        createChart(window.lastData);
    }

});