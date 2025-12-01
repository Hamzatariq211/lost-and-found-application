<?php
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get posted data
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if (
    !empty($data->full_name) &&
    !empty($data->username) &&
    !empty($data->email) &&
    !empty($data->mobile_number) &&
    !empty($data->password)
) {
    
    // Check if username already exists
    $check_query = "SELECT user_id FROM users WHERE username = :username OR email = :email";
    $check_stmt = $db->prepare($check_query);
    $check_stmt->bindParam(":username", $data->username);
    $check_stmt->bindParam(":email", $data->email);
    $check_stmt->execute();
    
    if ($check_stmt->rowCount() > 0) {
        http_response_code(400);
        echo json_encode([
            "success" => false,
            "message" => "Username or email already exists"
        ]);
        exit();
    }
    
    // Insert user
    $query = "INSERT INTO users (full_name, username, email, mobile_number, password) 
              VALUES (:full_name, :username, :email, :mobile_number, :password)";
    
    $stmt = $db->prepare($query);
    
    // Hash password
    $hashed_password = password_hash($data->password, PASSWORD_BCRYPT);
    
    // Bind values
    $stmt->bindParam(":full_name", $data->full_name);
    $stmt->bindParam(":username", $data->username);
    $stmt->bindParam(":email", $data->email);
    $stmt->bindParam(":mobile_number", $data->mobile_number);
    $stmt->bindParam(":password", $hashed_password);
    
    if ($stmt->execute()) {
        $user_id = $db->lastInsertId();
        
        // Generate authentication token
        $token = bin2hex(random_bytes(32));
        $expires_at = date('Y-m-d H:i:s', strtotime('+30 days'));
        
        $token_query = "INSERT INTO user_sessions (user_id, token, expires_at) 
                        VALUES (:user_id, :token, :expires_at)";
        $token_stmt = $db->prepare($token_query);
        $token_stmt->bindParam(":user_id", $user_id);
        $token_stmt->bindParam(":token", $token);
        $token_stmt->bindParam(":expires_at", $expires_at);
        $token_stmt->execute();
        
        http_response_code(201);
        echo json_encode([
            "success" => true,
            "message" => "User registered successfully",
            "data" => [
                "user_id" => $user_id,
                "username" => $data->username,
                "email" => $data->email,
                "full_name" => $data->full_name,
                "token" => $token
            ]
        ]);
    } else {
        http_response_code(500);
        echo json_encode([
            "success" => false,
            "message" => "Unable to register user"
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Incomplete data. Please fill all required fields"
    ]);
}
?>
