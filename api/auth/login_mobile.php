<?php
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get posted data
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if (!empty($data->mobile_number) && !empty($data->password)) {
    
    // Check user credentials
    $query = "SELECT user_id, username, email, full_name, mobile_number, password, is_active 
              FROM users 
              WHERE mobile_number = :mobile_number";
    
    $stmt = $db->prepare($query);
    $stmt->bindParam(":mobile_number", $data->mobile_number);
    $stmt->execute();
    
    if ($stmt->rowCount() > 0) {
        $row = $stmt->fetch(PDO::FETCH_ASSOC);
        
        // Verify password
        if (password_verify($data->password, $row['password'])) {
            
            if (!$row['is_active']) {
                http_response_code(403);
                echo json_encode([
                    "success" => false,
                    "message" => "Account is inactive"
                ]);
                exit();
            }
            
            // Generate authentication token
            $token = bin2hex(random_bytes(32));
            $expires_at = date('Y-m-d H:i:s', strtotime('+30 days'));
            
            $token_query = "INSERT INTO user_sessions (user_id, token, expires_at) 
                            VALUES (:user_id, :token, :expires_at)";
            $token_stmt = $db->prepare($token_query);
            $token_stmt->bindParam(":user_id", $row['user_id']);
            $token_stmt->bindParam(":token", $token);
            $token_stmt->bindParam(":expires_at", $expires_at);
            $token_stmt->execute();
            
            http_response_code(200);
            echo json_encode([
                "success" => true,
                "message" => "Login successful",
                "data" => [
                    "user_id" => $row['user_id'],
                    "username" => $row['username'],
                    "email" => $row['email'],
                    "full_name" => $row['full_name'],
                    "mobile_number" => $row['mobile_number'],
                    "token" => $token
                ]
            ]);
        } else {
            http_response_code(401);
            echo json_encode([
                "success" => false,
                "message" => "Invalid password"
            ]);
        }
    } else {
        http_response_code(404);
        echo json_encode([
            "success" => false,
            "message" => "Mobile number not registered"
        ]);
    }
} else {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Mobile number and password are required"
    ]);
}
?>
