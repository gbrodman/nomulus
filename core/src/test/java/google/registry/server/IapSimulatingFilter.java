// Copyright 2022 The Nomulus Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.server;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebToken;
import com.google.api.client.testing.http.FixedClock;
import com.google.auth.oauth2.TokenVerifier;
import com.google.auth.oauth2.TokenVerifier.VerificationException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class IapSimulatingFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest) {
      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String path = httpRequest.getServletPath();
      if (path.equals("/registrar")) {
        CookieAddingRequestWrapper wrapper = new CookieAddingRequestWrapper(httpRequest);
        wrapper.addCookie(new Cookie("X-Goog-IAP-JWT-Assertion", "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjBvZUxjUSJ9.eyJhdWQiOiIvcHJvamVjdHMvNzM0OTM1NTgzMDU3L2FwcHMvZG9tYWluLXJlZ2lzdHJ5LXFhIiwiYXpwIjoiL3Byb2plY3RzLzczNDkzNTU4MzA1Ny9hcHBzL2RvbWFpbi1yZWdpc3RyeS1xYSIsImVtYWlsIjoiZ2Jyb2RtYW5AZ29vZ2xlLmNvbSIsImV4cCI6MTY2OTkyNDI5MSwiZ29vZ2xlIjp7ImFjY2Vzc19sZXZlbHMiOlsiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9yZWNlbnRTZWN1cmVDb25uZWN0RGF0YSIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvdGVzdE5vT3AiLCJhY2Nlc3NQb2xpY2llcy81MTg1NTEyODA5MjQvYWNjZXNzTGV2ZWxzL2V2YXBvcmF0aW9uUWFEYXRhRnVsbHlUcnVzdGVkIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9jYWFfZGlzYWJsZWQiLCJhY2Nlc3NQb2xpY2llcy81MTg1NTEyODA5MjQvYWNjZXNzTGV2ZWxzL3JlY2VudE5vbk1vYmlsZVNlY3VyZUNvbm5lY3REYXRhIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9mdWxseVRydXN0ZWRfY2FuYXJ5RGF0YSIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvZnVsbHlUcnVzdGVkX3Byb2REYXRhIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9hY2NlcHRfYWxsIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9mcm9tX2NvcnBfaXBzIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9kZXZpY2VJbkludmVudG9yeV9wcm9kRGF0YSIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvY2hyb21lX3NpZ25lZF9pbl9wcm9maWxlIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9naV9wZXJtaXR0ZWRBY2Nlc3NfcHJvZERhdGEiLCJhY2Nlc3NQb2xpY2llcy81MTg1NTEyODA5MjQvYWNjZXNzTGV2ZWxzL25vdF9lbWJhcmdvZWRfYnlwYXNzIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9ub3RfZW1iYXJnb2VkX2FsbG93bGlzdCIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvbm90X2VtYmFyZ29lZCIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvbm90X2Jsb2NrbGlzdGVkX2RldmljZV9wcm9kRGF0YSJdLCJkZXZpY2VfaWQiOiJoU0o3VWJyR3JPTm5GY2tSaVJCa3ZkTTQ0a1o5ajVVNVpQOWs1LVNDbXhjIn0sImhkIjoiZ29vZ2xlLmNvbSIsImlhdCI6MTY2OTkyMzY5MSwiaXNzIjoiaHR0cHM6Ly9jbG91ZC5nb29nbGUuY29tL2lhcCIsInN1YiI6ImFjY291bnRzLmdvb2dsZS5jb206MTAxMDQwMjU0MzY2ODAzOTkwMTUzIn0.L2-86MDGhxHSHGFnj_w61-xCg1WeMqHpcngacQDWPhI3Xkvpcl8ui7wmOxnmWbtCpEi7dxXou1s30Hyekli4jQ"));
        chain.doFilter(wrapper, response);
        return;
      }
    }
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {

  }

  private static class CookieAddingRequestWrapper extends HttpServletRequestWrapper {

    private final ArrayList<Cookie> cookies = new ArrayList<>();

    public CookieAddingRequestWrapper(HttpServletRequest request) {
      super(request);
      if (request.getCookies() != null) {
        for (Cookie cookie : request.getCookies()) {
          cookies.add(cookie);
        }
      }
    }

    private void addCookie(Cookie cookie) {
      cookies.add(cookie);
    }

    @Override
    public Cookie[] getCookies() {
      return cookies.toArray(new Cookie[0]);
    }
  }

  public static void main(String[] args)
      throws GeneralSecurityException, IOException, VerificationException {
    String jwt = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjBvZUxjUSJ9.eyJhdWQiOiIvcHJvamVjdHMvNzM0OTM1NTgzMDU3L2FwcHMvZG9tYWluLXJlZ2lzdHJ5LXFhIiwiYXpwIjoiL3Byb2plY3RzLzczNDkzNTU4MzA1Ny9hcHBzL2RvbWFpbi1yZWdpc3RyeS1xYSIsImVtYWlsIjoiZ2Jyb2RtYW5AZ29vZ2xlLmNvbSIsImV4cCI6MTY2OTkyNDI5MSwiZ29vZ2xlIjp7ImFjY2Vzc19sZXZlbHMiOlsiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9yZWNlbnRTZWN1cmVDb25uZWN0RGF0YSIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvdGVzdE5vT3AiLCJhY2Nlc3NQb2xpY2llcy81MTg1NTEyODA5MjQvYWNjZXNzTGV2ZWxzL2V2YXBvcmF0aW9uUWFEYXRhRnVsbHlUcnVzdGVkIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9jYWFfZGlzYWJsZWQiLCJhY2Nlc3NQb2xpY2llcy81MTg1NTEyODA5MjQvYWNjZXNzTGV2ZWxzL3JlY2VudE5vbk1vYmlsZVNlY3VyZUNvbm5lY3REYXRhIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9mdWxseVRydXN0ZWRfY2FuYXJ5RGF0YSIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvZnVsbHlUcnVzdGVkX3Byb2REYXRhIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9hY2NlcHRfYWxsIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9mcm9tX2NvcnBfaXBzIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9kZXZpY2VJbkludmVudG9yeV9wcm9kRGF0YSIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvY2hyb21lX3NpZ25lZF9pbl9wcm9maWxlIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9naV9wZXJtaXR0ZWRBY2Nlc3NfcHJvZERhdGEiLCJhY2Nlc3NQb2xpY2llcy81MTg1NTEyODA5MjQvYWNjZXNzTGV2ZWxzL25vdF9lbWJhcmdvZWRfYnlwYXNzIiwiYWNjZXNzUG9saWNpZXMvNTE4NTUxMjgwOTI0L2FjY2Vzc0xldmVscy9ub3RfZW1iYXJnb2VkX2FsbG93bGlzdCIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvbm90X2VtYmFyZ29lZCIsImFjY2Vzc1BvbGljaWVzLzUxODU1MTI4MDkyNC9hY2Nlc3NMZXZlbHMvbm90X2Jsb2NrbGlzdGVkX2RldmljZV9wcm9kRGF0YSJdLCJkZXZpY2VfaWQiOiJoU0o3VWJyR3JPTm5GY2tSaVJCa3ZkTTQ0a1o5ajVVNVpQOWs1LVNDbXhjIn0sImhkIjoiZ29vZ2xlLmNvbSIsImlhdCI6MTY2OTkyMzY5MSwiaXNzIjoiaHR0cHM6Ly9jbG91ZC5nb29nbGUuY29tL2lhcCIsInN1YiI6ImFjY291bnRzLmdvb2dsZS5jb206MTAxMDQwMjU0MzY2ODAzOTkwMTUzIn0.L2-86MDGhxHSHGFnj_w61-xCg1WeMqHpcngacQDWPhI3Xkvpcl8ui7wmOxnmWbtCpEi7dxXou1s30Hyekli4jQ";
    HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
    TokenVerifier tokenVerifier =
        TokenVerifier.newBuilder().setAudience("/projects/734935583057/apps/domain-registry-qa").setIssuer("https://cloud.google.com/iap")
            .setClock(new FixedClock()).build();
    JsonWebToken jsonWebToken = tokenVerifier.verify(jwt);
    int x = 5;
    int y = x;
  }
}
