/*
 * Copyright 2025 0x746143
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("unused")

package x746143.ktntpg.pgclient.wire3

// https://www.postgresql.org/docs/16/protocol-message-formats.html
internal object BackendMessage {
    const val AUTHENTICATION = 'R'.code.toByte()
    const val BACKEND_KEY_DATA = 'K'.code.toByte()
    const val BIND_COMPLETE = '2'.code.toByte()
    const val CLOSE_COMPLETE = '3'.code.toByte()
    const val COMMAND_COMPLETE = 'C'.code.toByte()
    const val COPY_DATA = 'd'.code.toByte()
    const val COPY_DONE = 'c'.code.toByte()
    const val COPY_IN_RESPONSE = 'G'.code.toByte()
    const val COPY_OUT_RESPONSE = 'H'.code.toByte()
    const val COPY_BOTH_RESPONSE = 'W'.code.toByte()
    const val DATA_ROW = 'D'.code.toByte()
    const val EMPTY_QUERY_RESPONSE = 'I'.code.toByte()
    const val ERROR_RESPONSE = 'E'.code.toByte()
    const val FUNCTION_CALL_RESPONSE = 'V'.code.toByte()
    const val NEGOTIATE_PROTOCOL_VERSION = 'v'.code.toByte()
    const val NO_DATA = 'n'.code.toByte()
    const val NOTICE_RESPONSE = 'N'.code.toByte()
    const val NOTIFICATION_RESPONSE = 'A'.code.toByte()
    const val PARAMETER_DESCRIPTION = 't'.code.toByte()
    const val PARAMETER_STATUS = 'S'.code.toByte()
    const val PARSE_COMPLETE = '1'.code.toByte()
    const val PORTAL_SUSPENDED = 's'.code.toByte()
    const val READY_FOR_QUERY = 'Z'.code.toByte()
    const val ROW_DESCRIPTION = 'T'.code.toByte()
}

internal object FrontendMessage {
    const val BIND = 'B'.code
    const val CLOSE = 'C'.code
    const val COPY_DATA = 'd'.code
    const val COPY_DONE = 'c'.code
    const val COPY_FAIL = 'f'.code
    const val DESCRIBE = 'D'.code
    const val EXECUTE = 'E'.code
    const val FLUSH = 'H'.code
    const val FUNCTION_CALL = 'F'.code
    const val GSS_RESPONSE = 'p'.code
    const val PARSE = 'P'.code
    const val PASSWORD_MESSAGE = 'p'.code
    const val QUERY = 'Q'.code
    const val SASL_INITIAL_RESPONSE = 'p'.code
    const val SASL_RESPONSE = 'p'.code
    const val SYNC = 'S'.code
    const val TERMINATE = 'X'.code
}

internal object Authentication {
    const val OK = 0
    const val KERBEROS_V5 = 2
    const val CLEARTEXT_PASSWORD = 3
    const val MD5_PASSWORD = 5
    const val GSS = 7
    const val GSS_CONTINUE = 8
    const val SSPI = 9
    const val SASL = 10
    const val SASL_CONTINUE = 11
    const val SASL_FINAL = 12
}

internal object MessageCode {
    const val PROTOCOL_VERSION = 0x00030000
    const val CANCEL_REQUEST = 80877102
    const val SSL_REQUEST = 80877103
    const val GSS_ENC_REQUEST = 80877104
}

// https://www.postgresql.org/docs/16/protocol-error-fields.html
internal object ErrorField {
    const val SEVERITY = 'S'.code
    const val SEVERITY_EN = 'V'.code
    const val CODE = 'C'.code
    const val MESSAGE = 'M'.code
    const val DETAIL = 'D'.code
    const val HINT = 'H'.code
    const val POSITION = 'P'.code
    const val INTERNAL_POSITION = 'p'.code
    const val INTERNAL_QUERY = 'q'.code
    const val WHERE = 'W'.code
    const val SCHEMA_NAME = 's'.code
    const val TABLE_NAME = 't'.code
    const val COLUMN_NAME = 'c'.code
    const val DATA_TYPE_NAME = 'd'.code
    const val CONSTRAINT_NAME = 'n'.code
    const val FILE = 'F'.code
    const val LINE = 'L'.code
    const val ROUTINE = 'R'.code
}