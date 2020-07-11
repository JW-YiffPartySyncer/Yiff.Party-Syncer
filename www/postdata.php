<?php
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET, OPTIONS');
header('Access-Control-Max-Age: 1000');
header('Access-Control-Allow-Headers: Content-Type');

// EDIT DATA BELOW
$servername = "localhost";
$username = "[USERNAME]"; 
$password = "[PASSWORD]";
$dbname = "yiffparty"; // Table name. Should be yiffparty if you used the yiffparty.sql file

// Create connection
$conn = new mysqli($servername, $username, $password, $dbname);
// Check connection
if ($conn->connect_error) {
  die("Connection failed: " . $conn->connect_error);
}

//$data = get_post('data');
$data = $_POST['markup'];
$timestamp = $_POST['timestamp'];
$part = $_POST['part'];

$sql = "INSERT INTO webrip (timestamp, part, data) VALUES ('$timestamp', '$part', '$data')";

if ($conn->query($sql) === TRUE) {
  echo "New record created successfully";
} else {
  echo "Error: " . $sql . "<br>" . $conn->error;
}

$conn->close();
?>