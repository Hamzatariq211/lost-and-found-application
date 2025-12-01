<?php
include_once '../config/cors.php';
include_once '../config/database.php';

$database = new Database();
$db = $database->getConnection();

// Get JSON input
$data = json_decode(file_get_contents("php://input"));

// Validate required fields
if (!isset($data->user_id) || !isset($data->full_name) || !isset($data->username) ||
    !isset($data->email) || !isset($data->mobile_number)) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields"
    ]);
    exit();
}

$user_id = $data->user_id;
$full_name = $data->full_name;
$username = $data->username;
$email = $data->email;
$mobile_number = $data->mobile_number;

// Check if username is already taken by another user
$check_query = "SELECT user_id FROM users WHERE username = :username AND user_id != :user_id";
$check_stmt = $db->prepare($check_query);
$check_stmt->bindParam(":username", $username);
$check_stmt->bindParam(":user_id", $user_id);
$check_stmt->execute();

if ($check_stmt->rowCount() > 0) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Username already taken"
    ]);
    exit();
}

// Check if email is already taken by another user
$check_email_query = "SELECT user_id FROM users WHERE email = :email AND user_id != :user_id";
$check_email_stmt = $db->prepare($check_email_query);
$check_email_stmt->bindParam(":email", $email);
$check_email_stmt->bindParam(":user_id", $user_id);
$check_email_stmt->execute();

if ($check_email_stmt->rowCount() > 0) {
    http_response_code(400);
    echo json_encode([
        "success" => false,
        "message" => "Email already in use"
    ]);
    exit();
}

// Update user profile
$update_query = "UPDATE users
                 SET full_name = :full_name,
                     username = :username,
                     email = :email,
                     mobile_number = :mobile_number
                 WHERE user_id = :user_id";

$stmt = $db->prepare($update_query);
$stmt->bindParam(":full_name", $full_name);
$stmt->bindParam(":username", $username);
$stmt->bindParam(":email", $email);
$stmt->bindParam(":mobile_number", $mobile_number);
$stmt->bindParam(":user_id", $user_id);

if ($stmt->execute()) {
    // If password is provided, update it
    if (isset($data->password) && !empty($data->password)) {
        $password_hash = password_hash($data->password, PASSWORD_BCRYPT);
        $password_query = "UPDATE users SET password_hash = :password_hash WHERE user_id = :user_id";
        $password_stmt = $db->prepare($password_query);
        $password_stmt->bindParam(":password_hash", $password_hash);
        $password_stmt->bindParam(":user_id", $user_id);
        $password_stmt->execute();
    }

    http_response_code(200);
    echo json_encode([
        "success" => true,
        "message" => "Profile updated successfully",
        "data" => [
            "user_id" => $user_id,
            "full_name" => $full_name,
            "username" => $username,
            "email" => $email,
            "mobile_number" => $mobile_number
        ]
    ]);
} else {
    http_response_code(500);
    echo json_encode([
        "success" => false,
        "message" => "Failed to update profile"
    ]);
}
?>

