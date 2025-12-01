<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Lost & Found API - Documentation</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1000px;
            margin: 0 auto;
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #6C63FF 0%, #5548CC 100%);
            color: white;
            padding: 40px;
            text-align: center;
        }
        .header h1 { font-size: 2.5em; margin-bottom: 10px; }
        .header p { font-size: 1.2em; opacity: 0.9; }
        .content { padding: 40px; }
        .section {
            margin-bottom: 30px;
            padding-bottom: 30px;
            border-bottom: 1px solid #eee;
        }
        .section:last-child { border-bottom: none; }
        h2 {
            color: #6C63FF;
            margin-bottom: 15px;
            font-size: 1.8em;
        }
        .endpoint {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin: 10px 0;
            border-left: 4px solid #6C63FF;
        }
        .endpoint-title {
            font-weight: bold;
            color: #333;
            margin-bottom: 5px;
        }
        .method {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 4px;
            font-size: 0.85em;
            font-weight: bold;
            margin-right: 10px;
        }
        .method.get { background: #28a745; color: white; }
        .method.post { background: #007bff; color: white; }
        .method.put { background: #ffc107; color: black; }
        .method.delete { background: #dc3545; color: white; }
        .url {
            font-family: 'Courier New', monospace;
            color: #666;
            font-size: 0.9em;
        }
        .status {
            display: inline-block;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: bold;
            margin: 10px 5px;
        }
        .status.online { background: #d4edda; color: #155724; }
        .status.offline { background: #f8d7da; color: #721c24; }
        .btn {
            display: inline-block;
            padding: 12px 30px;
            background: #6C63FF;
            color: white;
            text-decoration: none;
            border-radius: 6px;
            margin: 10px 5px;
            transition: background 0.3s;
        }
        .btn:hover { background: #5548CC; }
        .features {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin: 20px 0;
        }
        .feature {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
        }
        .feature-icon {
            font-size: 2em;
            margin-bottom: 10px;
        }
        code {
            background: #f4f4f4;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
            color: #e83e8c;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🔍 Lost & Found API</h1>
            <p>RESTful API for Lost and Found Mobile Application</p>
            <div style="margin-top: 20px;">
                <?php
                // Check database connection
                try {
                    $conn = new PDO("mysql:host=localhost;dbname=lost_and_found_db", "root", "");
                    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
                    echo '<span class="status online">✓ Database Connected</span>';
                    
                    // Get stats
                    $users = $conn->query("SELECT COUNT(*) FROM users")->fetchColumn();
                    $posts = $conn->query("SELECT COUNT(*) FROM posts")->fetchColumn();
                    echo '<span class="status online">' . $users . ' Users</span>';
                    echo '<span class="status online">' . $posts . ' Posts</span>';
                } catch(PDOException $e) {
                    echo '<span class="status offline">✗ Database Offline</span>';
                }
                ?>
            </div>
        </div>

        <div class="content">
            <div class="section">
                <h2>📋 Quick Start</h2>
                <p>Welcome to the Lost & Found API. This API provides endpoints for user authentication, post management, and notifications.</p>
                <div style="margin-top: 20px;">
                    <a href="test_api.html" class="btn">🧪 Test API</a>
                    <a href="README.md" class="btn">📖 Documentation</a>
                </div>
            </div>

            <div class="section">
                <h2>✨ Features</h2>
                <div class="features">
                    <div class="feature">
                        <div class="feature-icon">🔐</div>
                        <strong>Authentication</strong>
                        <p>Token-based auth with email/mobile login</p>
                    </div>
                    <div class="feature">
                        <div class="feature-icon">📝</div>
                        <strong>Post Management</strong>
                        <p>Create, read, and search posts</p>
                    </div>
                    <div class="feature">
                        <div class="feature-icon">🖼️</div>
                        <strong>Image Upload</strong>
                        <p>Upload and retrieve item images</p>
                    </div>
                    <div class="feature">
                        <div class="feature-icon">🔔</div>
                        <strong>Notifications</strong>
                        <p>Push notification support</p>
                    </div>
                    <div class="feature">
                        <div class="feature-icon">🔄</div>
                        <strong>Offline Sync</strong>
                        <p>Sync offline data to cloud</p>
                    </div>
                    <div class="feature">
                        <div class="feature-icon">🔍</div>
                        <strong>Search & Filter</strong>
                        <p>Advanced search capabilities</p>
                    </div>
                </div>
            </div>

            <div class="section">
                <h2>🔌 API Endpoints</h2>
                
                <h3 style="margin: 20px 0 10px 0; color: #333;">Authentication</h3>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method post">POST</span>
                        <span class="url">/auth/signup.php</span>
                    </div>
                    <p>Register a new user account</p>
                </div>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method post">POST</span>
                        <span class="url">/auth/login.php</span>
                    </div>
                    <p>Login with username/email and password</p>
                </div>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method post">POST</span>
                        <span class="url">/auth/login_mobile.php</span>
                    </div>
                    <p>Login with mobile number and password</p>
                </div>

                <h3 style="margin: 20px 0 10px 0; color: #333;">Posts</h3>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method get">GET</span>
                        <span class="url">/posts/get_posts.php</span>
                    </div>
                    <p>Get all posts with optional filters (item_type, search)</p>
                </div>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method get">GET</span>
                        <span class="url">/posts/get_post_by_id.php</span>
                    </div>
                    <p>Get a specific post by ID</p>
                </div>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method post">POST</span>
                        <span class="url">/posts/create_post.php</span>
                    </div>
                    <p>Create a new post (requires authentication)</p>
                </div>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method post">POST</span>
                        <span class="url">/posts/sync_posts.php</span>
                    </div>
                    <p>Sync offline posts to cloud (requires authentication)</p>
                </div>

                <h3 style="margin: 20px 0 10px 0; color: #333;">Notifications</h3>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method get">GET</span>
                        <span class="url">/notifications/get_notifications.php</span>
                    </div>
                    <p>Get user notifications (requires authentication)</p>
                </div>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method post">POST</span>
                        <span class="url">/notifications/create_notification.php</span>
                    </div>
                    <p>Create a new notification (requires authentication)</p>
                </div>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method post">POST</span>
                        <span class="url">/notifications/mark_as_read.php</span>
                    </div>
                    <p>Mark notification as read (requires authentication)</p>
                </div>

                <h3 style="margin: 20px 0 10px 0; color: #333;">User</h3>
                <div class="endpoint">
                    <div class="endpoint-title">
                        <span class="method get">GET</span>
                        <span class="url">/user/get_profile.php</span>
                    </div>
                    <p>Get user profile information (requires authentication)</p>
                </div>
            </div>

            <div class="section">
                <h2>🔧 Setup Instructions</h2>
                <ol style="line-height: 2;">
                    <li>Import <code>database/lost_and_found.sql</code> into phpMyAdmin</li>
                    <li>Copy API folder to <code>C:\xampp\htdocs\lost_and_found_api\</code></li>
                    <li>Create <code>uploads</code> folder for image storage</li>
                    <li>Update database credentials in <code>config/database.php</code></li>
                    <li>Start Apache and MySQL in XAMPP</li>
                    <li>Test API using the test page or Postman</li>
                </ol>
            </div>

            <div class="section">
                <h2>📱 Android Integration</h2>
                <p>Add these dependencies to your <code>build.gradle.kts</code>:</p>
                <ul style="line-height: 2; margin-top: 10px;">
                    <li>Retrofit 2.9.0 (for API calls)</li>
                    <li>Room Database (for offline storage)</li>
                    <li>Glide (for image loading)</li>
                    <li>Coroutines (for async operations)</li>
                </ul>
            </div>

            <div class="section" style="text-align: center; border: none;">
                <p style="color: #666;">
                    Lost & Found API v1.0 | Built for XAMPP | 
                    <a href="https://github.com" style="color: #6C63FF;">GitHub</a>
                </p>
            </div>
        </div>
    </div>
</body>
</html>
