// ==UserScript==
// @name         yiff.party
// @namespace    *yiff.party*
// @version      0.1
// @description  Helper tool to sync Yiff.Party stuff
// @author       JW
// @match        *yiff.party*
// @connect      self
// @grant        none
// ==/UserScript==

var d = new Date();
var currentTime = d.getTime();
var counter = 0;
var running = false;

/*
	Pauses execution of the script for a given amount of ms
	Needs to be called like "await wait(ms)"
*/
function wait(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function loop() {
	d = new Date();
	currentTime = d.getTime();
	counter++
	if(counter > 60 && !running) {
		running = true;
		counter = 0;
	}
	
	if(running) {
		if(counter == 0) {
			scanRecentActivity();
		}
		if(counter == 300) {
			location.reload();
		}
	}
	console.log(counter);
    setTimeout(loop, 1000);
}

	
/*
*/
function scanRecentActivity() {
	console.log("scanRecentActivity");
	var markup = document.documentElement.innerHTML;
	console.log(markup);
	
	var splitted = markup.split("\n");
	
	var i = 0;
	var counter = 1;
	try {
		for( i = 0; i < splitted.length; i++) {
			$.ajax({
			type : "POST",  //type of method
			url  : "http://localhost/postdata.php",  //your page
			crossDomain: true,
			data : { markup : splitted[i], part : i, timestamp : currentTime },// passing the values
			success: function(res){  
				console.log(counter + "/" + splitted.length);
				}
			});
			counter++;
		}
	} catch (e) {
		console.log(e);
	}
	
}

/*
	Runs stuff once before script execution
	TODO: this stuff doesnt work...
*/
function runOnce() {
	console.log("yiff.party runOnce");
	
	loop();
}

console.log("yiff.party helper loaded");
console.log("Waiting till page fully loaded");
window.addEventListener('load', function () {
  runOnce();
})