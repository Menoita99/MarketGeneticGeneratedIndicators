
var chartCount = 0;

function renderChart(type,title,xAxys,dataseries){
	try{
		var chartId = addNewChartContainer();
		let options = {
					  chart: {
						animations: {
				        	enabled: false,
						},
					    type: type,
				    	toolbar: {
				            tools: {
				              download: false
				            }
				    	}
					  },
					  series: dataseries,
					  xaxis: {
					    type: xAxys
					  },
					  title: {
				          text: title,
				          align: 'left'
				      },
					stroke: {
					      curve: 'smooth',
					      width: 2,
					}
				}
        new ApexCharts(document.querySelector("#"+chartId), options).render();
	}catch (e) {
	   console.log(e);
	}
}

function addNewChartContainer(){
	let chartId = "chart"+chartCount;
	let html = `
			<div class="row">
			<div class="col" style="max-height: 600px">
				<div class="card shadow mb-4">
					<div class="card-header d-flex justify-content-between align-items-center">
						<h6 class="text-primary fw-bold m-0">Chart</h6>
						<div class="dropdown no-arrow">
							<button class="btn btn-link btn-sm dropdown-toggle"
								aria-expanded="false" data-bs-toggle="dropdown" type="button">
								<i class="fas fa-ellipsis-v text-gray-400"></i>
							</button>
						</div>
					</div>
					<div class="card-body">
						<div id="#{id}"></div>
					</div>
				</div>
			</div>
		</div>
	`.replace("#{id}",chartId);
	let chartContainer = document.getElementById("chartContainer");
	chartContainer.innerHTML += html;
	chartCount++;
	return chartId;
}