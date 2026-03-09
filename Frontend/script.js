let chart;

async function uploadFile(){

const file=document.getElementById("fileInput").files[0];

const formData=new FormData();
formData.append("file",file);

await fetch("https://dashgen-ai.onrender.com/upload",{
method:"POST",
body:formData
});

alert("File uploaded successfully");

}

async function generateDashboard() {

    const question = document.getElementById("question").value;

    const response = await fetch("https://dashgen-ai.onrender.com/generate-dashboard", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ question: question })
    });

    const result = await response.json();

    const data = result.data;

    displaySummary(data.summary);
    displayAdvice(data.call_to_action);
    displayTable(data.table_data);
    displayChart(data.chart_type, data.table_data);
}

function displayAdvice(advice){
    document.getElementById("advice").innerHTML =
        "<b>AI Recommendation:</b> " + advice;
}

function displaySummary(summary) {
    document.getElementById("summary").innerText = summary;
}


function displayTable(data) {

    const container = document.getElementById("table-container");

    if (data.length === 0) {
        container.innerHTML = "No Data";
        return;
    }

    const keys = Object.keys(data[0]);

    let table = "<table><tr>";

    keys.forEach(k => {
        table += `<th>${k}</th>`;
    });

    table += "</tr>";

    data.forEach(row => {
        table += "<tr>";
        keys.forEach(k => {
            table += `<td>${row[k]}</td>`;
        });
        table += "</tr>";
    });

    table += "</table>";

    container.innerHTML = table;
}


function displayChart(type, data) {

    const ctx = document.getElementById("chartCanvas");

    if (chart) {
        chart.destroy();
    }

    const keys = Object.keys(data[0]);

    const labels = data.map(row => row[keys[0]]);
    const values = data.map(row => row[keys[1]]);

    let chartType = "bar";

    if (type.toLowerCase().includes("pie")) {
        chartType = "pie";
    }
    else if (type.toLowerCase().includes("line")) {
        chartType = "line";
    }
    else if (type.toLowerCase().includes("bar")) {
        chartType = "bar";
    }

    chart = new Chart(ctx, {

        type: chartType,

        data: {
            labels: labels,

            datasets: [{
                label: keys[1],
                data: values
            }]
        },

        options: {
            responsive: true
        }

    });
}