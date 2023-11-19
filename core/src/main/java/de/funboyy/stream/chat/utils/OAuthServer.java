package de.funboyy.stream.chat.utils;

import de.funboyy.stream.chat.StreamChatAddon;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import net.labymod.api.Laby;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.util.io.web.request.Request.Method;
import net.labymod.api.util.io.web.request.Response;
import net.labymod.api.util.io.web.request.types.StringRequest;

@SuppressWarnings("CallToPrintStackTrace")
public class OAuthServer {

  /**
   * This class was inspired by
   * {@code net.labymod.accountmanager.authentication.microsoft.oauth.OAuthServer}
   * **/

  private static final String CLIENT_ID = "5ipavdkl5wjev5rfqk9wge409vteyp";
  private static final int REDIRECT_PORT = 8087;
  private static final String REDIRECT_URL = String.format("http://localhost:%s", REDIRECT_PORT);
  private static final String SCOPES = String.join("+", "chat:read", "chat:edit");
  public static final String AUTHORIZE_URL = String.format("https://id.twitch.tv/oauth2/authorize"
      + "?response_type=token&client_id=%s&redirect_uri=%s&scope=%s", CLIENT_ID, REDIRECT_URL, SCOPES);
  public static final String REVOKE_URL = "https://id.twitch.tv/oauth2/revoke?client_id=%s&token=%s";

  private final ServerSocket serverSocket;
  private final ExecutorService executor;

  public OAuthServer() throws IOException {
    this.serverSocket = new ServerSocket(REDIRECT_PORT);
    this.executor = Executors.newSingleThreadExecutor();
  }

  public void listenForCodeAsync(final Consumer<String> callback) {
    this.executor.execute(() -> callback.accept(this.listenForCode()));
  }

  /**
   * Contains a workaround to get the token from URL, but because the token is in the hash of the
   * redirect from Twitch I'm redirecting the hash to the query so the server can access the token.
   * <p><a href="https://dev.twitch.tv/docs/authentication/getting-tokens-oauth/#implicit-grant-flow">
   * Click here for information about the token</a></p>
   * **/
  public String listenForCode() {
    while (this.serverSocket.isBound() && !this.serverSocket.isClosed()) {
      try (final Socket socket = this.serverSocket.accept();
          final Scanner scanner = new Scanner(socket.getInputStream());
          final BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream())) {
        final String path = scanner.nextLine().split(" ")[1];

        if (path.contains("?error=")) {
          sendResponse(output);
          return null;
        }

        if (!path.startsWith("/token")) {
          output.write("HTTP/1.0 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
          output.write("Content-Type: html; charset=UTF-8\r\n".getBytes(StandardCharsets.UTF_8));
          output.write("Connection: close\r\n".getBytes(StandardCharsets.UTF_8));
          output.write("\r\n".getBytes(StandardCharsets.UTF_8));

          final InputStream input = ResourceLocation.create(StreamChatAddon.getInstance()
                  .addonInfo().getNamespace(), "files/index.html").openStream();

          final byte[] buffer = new byte[8192];
          int read;
          while ((read = input.read(buffer, 0, 8192)) >= 0) {
            output.write(buffer, 0, read);
          }

          input.close();
          output.flush();
          continue;
        }

        if (path.contains("=") && path.contains("?access_token=") && path.contains("&")) {
          sendResponse(output);
          return path.substring(path.indexOf("=") + 1, path.indexOf("&"));
        }

      } catch (final Exception exception) {
        exception.printStackTrace();
        break;
      }
    }

    this.close();
    return null;
  }

  private void sendResponse(final BufferedOutputStream output) throws IOException {
    output.write("HTTP/1.0 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
    output.write("Content-Type: html; charset=UTF-8\r\n".getBytes(StandardCharsets.UTF_8));
    output.write("Connection: close\r\n".getBytes(StandardCharsets.UTF_8));
    output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    output.write("You can close this window now".getBytes(StandardCharsets.UTF_8));
    output.flush();

    this.close();
  }

  public void close() {
    try {
      if (!this.serverSocket.isClosed()) {
        this.serverSocket.close();
      }
    } catch (final IOException exception) {
      exception.printStackTrace();
    }
  }

  public static boolean revoke() {
    final StringRequest request = StringRequest.create().method(Method.POST).url(String.format(REVOKE_URL,
        CLIENT_ID, StreamChatAddon.getInstance().configuration().tokenConfig().token().get()));
    final Response<?> response = Laby.references().webResolver().resolveConnection(request);

    return response.getStatusCode() == 200;
  }

}
