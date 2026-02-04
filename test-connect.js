async function testConnection() {
    console.log('Testing connection to http://localhost:8080/api/auth/google...');
    try {
        const response = await fetch('http://localhost:8080/api/auth/google', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ token: 'test-token' })
        });
        console.log('Status:', response.status);
        const text = await response.text();
        console.log('Response:', text);
    } catch (error) {
        console.error('Connection failed:', error);
        if (error.cause) console.error('Cause:', error.cause);
    }
}

testConnection();
