package com.example.base.ui.base

import android.app.Application
import android.content.Context
import com.example.base.resources.R
import com.example.base.shared.error.AppException
import com.example.base.shared.error.NetworkErrorKind
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class UiErrorMessageMapperTest {

    private val mapper = DefaultUiErrorMessageMapper()
    private val context: Context
        get() = RuntimeEnvironment.getApplication()

    @Test
    fun mapsNetworkErrorKindsCorrectly() {
        val noInternet = mapper.mapToUiText(AppException.NetworkError(kind = NetworkErrorKind.NoInternet))
        assertIs<UiText.StringResource>(noInternet)
        assertEquals(R.string.error_network_no_internet, noInternet.resId)

        val timeout = mapper.mapToUiText(AppException.NetworkError(kind = NetworkErrorKind.Timeout))
        assertIs<UiText.StringResource>(timeout)
        assertEquals(R.string.error_network_timeout, timeout.resId)

        val connection = mapper.mapToUiText(AppException.NetworkError(kind = NetworkErrorKind.Connection))
        assertIs<UiText.StringResource>(connection)
        assertEquals(R.string.error_network_connection, connection.resId)
    }

    @Test
    fun mapsServerErrorWithAndWithoutCode() {
        val server500 = mapper.mapToUiText(AppException.ServerError(httpCode = 500))
        assertIs<UiText.StringResource>(server500)
        assertEquals(R.string.error_server_with_code, server500.resId)
        assertEquals(listOf(500), server500.args)

        val serverGeneric = mapper.mapToUiText(AppException.ServerError(httpCode = null))
        assertIs<UiText.StringResource>(serverGeneric)
        assertEquals(R.string.error_server_generic, serverGeneric.resId)
    }

    @Test
    fun mapsValidationErrorsByCodeAndField() {
        val emptyTitle = mapper.mapToUiText(AppException.ValidationError(code = "EMPTY_TITLE"))
        assertIs<UiText.StringResource>(emptyTitle)
        assertEquals(R.string.error_validation_empty_title, emptyTitle.resId)

        val titleField = mapper.mapToUiText(AppException.ValidationError(field = "title"))
        assertIs<UiText.StringResource>(titleField)
        assertEquals(R.string.error_validation_empty_title, titleField.resId)

        val customValidation = mapper.mapToUiText(AppException.ValidationError(message = "Password too short"))
        assertIs<UiText.DynamicString>(customValidation)
        assertEquals("Password too short", customValidation.value)

        val genericValidation = mapper.mapToUiText(AppException.ValidationError())
        assertIs<UiText.StringResource>(genericValidation)
        assertEquals(R.string.error_validation_generic, genericValidation.resId)
    }

    @Test
    fun mapsParseAndSessionErrors() {
        val parse = mapper.mapToUiText(AppException.ParseError())
        assertIs<UiText.StringResource>(parse)
        assertEquals(R.string.error_parse, parse.resId)

        val session = mapper.mapToUiText(AppException.RefreshTokenFailed())
        assertIs<UiText.StringResource>(session)
        assertEquals(R.string.session_expired, session.resId)
    }

    @Test
    fun mapsUnknownErrors() {
        val unknown = mapper.mapToUiText(AppException.UnknownError())
        assertIs<UiText.StringResource>(unknown)
        assertEquals(R.string.error_unknown, unknown.resId)

        val customUnknown = mapper.mapToUiText(AppException.UnknownError(message = "Specific native crash"))
        assertIs<UiText.DynamicString>(customUnknown)
        assertEquals("Specific native crash", customUnknown.value)
    }

    @Test
    fun resolvesActualLocalizedStringsWithContext() {
        val contextMapper = DefaultUiErrorMessageMapper(context)

        val noInternetString = contextMapper.map(AppException.NetworkError(kind = NetworkErrorKind.NoInternet))
        assertTrue(noInternetString.isNotBlank())
        assertEquals("No internet connection.", noInternetString)

        val server500String = contextMapper.map(AppException.ServerError(httpCode = 503))
        assertEquals("Server error (503). Please try again later.", server500String)

        val emptyTitleString = contextMapper.map(AppException.ValidationError(code = "EMPTY_TITLE"))
        assertEquals("Title cannot be empty!", emptyTitleString)
    }
}
