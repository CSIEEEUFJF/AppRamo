package com.ramoieeeufjf.appRamo.pages

object DoorInputParser {
    fun parseDelayMinutes(input: String): Long {
        val minutes = input.trim().toLongOrNull()
        require(minutes != null && minutes > 0 && minutes <= 1440) {
            "O atraso deve ficar entre 1 e 1440 minutos."
        }
        return minutes
    }

    fun parseProfileIndices(input: String): List<Int> {
        val tokens = splitListInput(input)
        require(tokens.isNotEmpty()) {
            "Informe ao menos um índice de perfil."
        }
        return tokens.map { token ->
            val value = token.toIntOrNull()
            require(value != null && value >= 0) {
                "Use apenas índices de perfil válidos."
            }
            value
        }
    }

    fun parseWeekdays(input: String): List<Int>? {
        val tokens = splitListInput(input)
        if (tokens.isEmpty()) {
            return null
        }
        return tokens.map { token ->
            val value = token.toIntOrNull()
            require(value != null && value in 0..6) {
                "Dias da semana devem ficar entre 0 e 6."
            }
            value
        }
    }

    fun parseCancelId(input: String): Long? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val id = trimmed.toLongOrNull()
        require(id != null && id > 0) {
            "ID de cancelamento inválido."
        }
        return id
    }

    private fun splitListInput(input: String): List<String> {
        return input.split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
    }
}
