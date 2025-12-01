<?php
header('Access-Control-Allow-Origin: *');
header('Content-Type: application/json');

echo json_encode([
    'success' => true,
    'message' => 'API is working!',
    'server_ip' => $_SERVER['SERVER_ADDR'],
    'client_ip' => $_SERVER['REMOTE_ADDR'],
    'timestamp' => date('Y-m-d H:i:s')
]);
?>

