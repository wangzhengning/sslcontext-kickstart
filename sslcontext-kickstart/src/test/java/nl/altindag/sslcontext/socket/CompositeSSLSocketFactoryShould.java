package nl.altindag.sslcontext.socket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CompositeSSLSocketFactoryShould {

    private final SSLParameters sslParameters = spy(
            new SSLParameters(
                    new String[] {"TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384"},
                    new String[] {"TLSv1.2"}
            )
    );

    private final SSLSocketFactory sslSocketFactory = mock(SSLSocketFactory.class);

    private final CompositeSSLSocketFactory victim = new CompositeSSLSocketFactory(sslSocketFactory, sslParameters);

    @Test
    void returnDefaultCipherSuites() {
        String[] defaultCipherSuites = victim.getDefaultCipherSuites();

        assertThat(defaultCipherSuites).containsExactly("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384");
        verify(sslParameters, times(1)).getCipherSuites();
    }

    @Test
    void returnSupportedCipherSuites() {
        String[] supportedCipherSuites = victim.getSupportedCipherSuites();

        assertThat(supportedCipherSuites).containsExactly("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384");
        verify(sslParameters, times(1)).getCipherSuites();
    }

    @Test
    void createSocket() throws IOException {
        SSLSocket mockedSslSocket = mock(SSLSocket.class);

        doReturn(mockedSslSocket).when(sslSocketFactory).createSocket();

        Socket socket = victim.createSocket();

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket();
        verify(mockedSslSocket, times(1)).setSSLParameters(sslParameters);
    }

    @Test
    void createSocketDoesNotUseSslParametersWhenInnerSslSocketFactoryReturnsSocket() throws IOException {
        Socket mockedSocket = mock(Socket.class);

        doReturn(mockedSocket).when(sslSocketFactory).createSocket();

        Socket socket = victim.createSocket();

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket();
        verifyNoInteractions(mockedSocket);
    }

    @Test
    void createSocketWithSocketInputStreamAutoClosable() throws IOException {
        Socket baseSocket = mock(SSLSocket.class);
        SSLSocket mockedSslSocket = mock(SSLSocket.class);
        InputStream inputStream = new ByteArrayInputStream(new byte[]{});

        doReturn(mockedSslSocket)
                .when(sslSocketFactory).createSocket(any(Socket.class), any(InputStream.class), anyBoolean());

        Socket socket = victim.createSocket(baseSocket, inputStream, true);

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket(baseSocket, inputStream, true);
        verify(mockedSslSocket, times(1)).setSSLParameters(sslParameters);
    }

    @Test
    void createSocketWithSocketHostPortAutoClosable() throws IOException {
        Socket baseSocket = mock(SSLSocket.class);
        SSLSocket mockedSslSocket = mock(SSLSocket.class);

        doReturn(mockedSslSocket)
                .when(sslSocketFactory).createSocket(any(Socket.class), anyString(), anyInt(), anyBoolean());

        Socket socket = victim.createSocket(baseSocket, "localhost", 8443, true);

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket(baseSocket, "localhost", 8443, true);
        verify(mockedSslSocket, times(1)).setSSLParameters(sslParameters);
    }

    @Test
    void createSocketWithHostPort() throws IOException {
        SSLSocket mockedSslSocket = mock(SSLSocket.class);

        doReturn(mockedSslSocket)
                .when(sslSocketFactory).createSocket(anyString(), anyInt());

        Socket socket = victim.createSocket("localhost", 8443);

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket("localhost", 8443);
        verify(mockedSslSocket, times(1)).setSSLParameters(sslParameters);
    }

    @Test
    void createSocketWithHostPortLocalAddressLocalPort() throws IOException {
        SSLSocket mockedSslSocket = mock(SSLSocket.class);

        doReturn(mockedSslSocket)
                .when(sslSocketFactory).createSocket(anyString(), anyInt(), any(InetAddress.class), anyInt());

        Socket socket = victim.createSocket("localhost", 8443, InetAddress.getLocalHost(), 1234);

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket("localhost", 8443, InetAddress.getLocalHost(), 1234);
        verify(mockedSslSocket, times(1)).setSSLParameters(sslParameters);
    }

    @Test
    void createSocketWithAddressPort() throws IOException {
        SSLSocket mockedSslSocket = mock(SSLSocket.class);

        doReturn(mockedSslSocket)
                .when(sslSocketFactory).createSocket(any(InetAddress.class), anyInt());

        Socket socket = victim.createSocket(InetAddress.getLocalHost(), 1234);

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket(InetAddress.getLocalHost(), 1234);
        verify(mockedSslSocket, times(1)).setSSLParameters(sslParameters);
    }

    @Test
    void createSocketWithAddressPortLocalAddressPort() throws IOException {
        SSLSocket mockedSslSocket = mock(SSLSocket.class);

        doReturn(mockedSslSocket)
                .when(sslSocketFactory).createSocket(any(InetAddress.class), anyInt(), any(InetAddress.class), anyInt());

        Socket socket = victim.createSocket(InetAddress.getLocalHost(), 1234, InetAddress.getLocalHost(), 4321);

        assertThat(socket).isNotNull();
        verify(sslSocketFactory, times(1)).createSocket(InetAddress.getLocalHost(), 1234, InetAddress.getLocalHost(), 4321);
        verify(mockedSslSocket, times(1)).setSSLParameters(sslParameters);
    }

}
