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



async function sendQuery(){

const query=document.getElementById("queryInput").value;

const response=await fetch("http://localhost:5000/query",{

method:"POST",

headers:{
"Content-Type":"application/json"
},

body:JSON.stringify({query:query})

});

const data=await response.json();

const recommended = recommendChart(data);

document.getElementById("recommendedChart").innerText = recommended;

document.getElementById("chartType").value = recommended;

window.lastData = data;

createTable(data);

createChart(data);

}



function createTable(data){

const tableHead=document.querySelector("#resultTable thead");

const tableBody=document.querySelector("#resultTable tbody");

tableHead.innerHTML="";
tableBody.innerHTML="";

const columns=Object.keys(data[0]);

let headRow="<tr>";

columns.forEach(col=>{

headRow+=`<th>${col}</th>`;

});

headRow+="</tr>";

tableHead.innerHTML=headRow;

data.forEach(row=>{

let tr="<tr>";

columns.forEach(col=>{

tr+=`<td>${row[col]}</td>`;

});

tr+="</tr>";

tableBody.innerHTML+=tr;

});

}



function createChart(data){

const labels = data.map(d => d.name);
const values = data.map(d => d.value);

const chartType = document.getElementById("chartType").value;

const ctx = document.getElementById("chartCanvas");

if(chart){
chart.destroy();
}

chart = new Chart(ctx,{

type: chartType,

data:{

labels: labels,

datasets:[{

label: "Data",

data: values,

backgroundColor:[
"#2563eb",
"#22c55e",
"#f59e0b",
"#ef4444",
"#8b5cf6",
"#14b8a6"
],

borderColor:"#1e293b",
borderWidth:1

}]

},

options:{

responsive:true,
maintainAspectRatio:false,
animation:{
duration:1000,
easing:"easeOutQuart"
}

}

});

}

function recommendChart(data){

const count = data.length;

if(count <= 5){
return "pie";
}

if(count <= 10){
return "bar";
}

return "line";

}

document.getElementById("chartType").addEventListener("change", function(){

if(window.lastData){
createChart(window.lastData);
}

});