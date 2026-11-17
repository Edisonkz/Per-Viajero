package pe.edu.utp.perviajero

import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.utp.perviajero.data.common.ErrorClassifier
import pe.edu.utp.perviajero.data.common.NetworkErrorType
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorClassifierTest {

    @Test
    fun classifyThrowable_mapsExpectedExceptions() {
        assertEquals(NetworkErrorType.TIMEOUT, ErrorClassifier.classifyThrowable(SocketTimeoutException()).first)
        assertEquals(NetworkErrorType.NO_INTERNET, ErrorClassifier.classifyThrowable(UnknownHostException()).first)
        assertEquals(NetworkErrorType.PARSE_ERROR, ErrorClassifier.classifyThrowable(JsonSyntaxException("bad")).first)
    }

    @Test
    fun classifyHttpCode_mapsClientAndServerErrors() {
        assertEquals(NetworkErrorType.SERVER_ERROR, ErrorClassifier.classifyHttpCode(503).first)
        assertEquals(NetworkErrorType.SERVER_ERROR, ErrorClassifier.classifyHttpCode(404).first)
    }
}
