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
package x746143.ktntpg.pgclient

class PgException(message: String, val severity: String? = null, val code: String? = null) : Exception() {
    private val _message = message
    override val message: String
        get() = buildString {
            if (severity != null) {
                append(severity).append(" ")
            }
            if (code != null) {
                append("[$code] ")
            }
            append(_message)
        }
}