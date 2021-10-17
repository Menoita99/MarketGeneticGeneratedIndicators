
//renderLineChart('teste','numeric',JSON.parse("[{\"data\":[{\"x\":6,\"y\":8},{\"x\":5,\"y\":9},{\"x\":4,\"y\":9},{\"x\":3,\"y\":8},{\"x\":2,\"y\":6},{\"x\":1,\"y\":5},{\"x\":0,\"y\":1},{\"x\":9,\"y\":10},{\"x\":8,\"y\":7},{\"x\":7,\"y\":1}],\"name\":\"values\"}]"));

function renderLineChart(title,xaxys,dataseries){
	try{
		let options = {
				  chart: {
				    type: 'line',
			    	toolbar: {
			            tools: {
			              download: false
			            }
			    	}
				  },

				  series: dataseries,
				  xaxis: {
				    type: xaxys
				  },
				  title: {
			          text: title,
			          align: 'left'
			        },
				}
        var chart = new ApexCharts(document.querySelector("#chart"), options);
        chart.render();
	}catch (e) {
	   console.log(e);
	}
}