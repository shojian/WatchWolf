var Client = require('ftp');
var fs = require('fs'); 

var watchedDir = process.argv[2];
var files = fs.readdirSync(watchedDir);
var toSplice = [];
files.forEach(function (val, index, array) {
	var path = "/";
	if ((val[0] != ".") && (val != "README.md") && (val != "copyright") && (val != "nbproject")) {
		if (val == "imgs") {
			console.log("alarm");
		}
		if (fs.statSync(watchedDir+path+val).isFile()) {
			
		}
		if (fs.statSync(watchedDir+path+val).isDirectory()) {
			path += val+"/";
			toSplice.push(index);
			var subfolderFiles = fs.readdirSync(watchedDir+path);
			subfolderFiles.forEach(function (tempVal, tempIndex, tempArray) {
				array.push(path+tempVal);
			});
		}
	}
});
toSplice.reverse().forEach(function (val, index, array) {
	files.splice(val, 1);
});