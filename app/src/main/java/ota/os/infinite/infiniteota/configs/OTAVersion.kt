package ota.os.infinite.infiniteota.configs

import android.content.Context
import ota.os.infinite.infiniteota.utils.OTAUtils

import java.text.ParseException
import java.util.regex.Pattern

object OTAVersion {

    private val UNAME_R = "uname -r"

    fun getFullLocalVersion(context: Context): String {
        val source = OTAConfig.getInstance(context).versionSource
        val sourceString: String
        sourceString = if (source.equals(UNAME_R, ignoreCase = true)) {
            OTAUtils.runCommand(UNAME_R)
        } else {
            OTAUtils.getBuildProp(source)
        }
        return sourceString
    }

    fun checkServerVersion(serverVersion: String, context: Context): Boolean {
        var serverVersions = serverVersion
        var localVersion = getFullLocalVersion(context)
        localVersion = extractVersionFrom(localVersion, context)
        serverVersions = extractVersionFrom(serverVersions, context)

        OTAUtils.logInfo("serverVersion: " + serverVersions)
        OTAUtils.logInfo("localVersion: " + localVersion)

        return compareVersion(serverVersions, localVersion, context)
    }


    fun compareVersion(serverVersion: String, localVersion: String, context: Context): Boolean {
        var versionIsNew = false

        if (serverVersion.isEmpty() || localVersion.isEmpty()) {
            return false
        }

        val format = OTAConfig.getInstance(context).format
        if (format == null) {
            try {
                val serverNumber = Integer.parseInt(serverVersion.replace("[\\D]".toRegex(), ""))
                val currentNumber = Integer.parseInt(localVersion.replace("[\\D]".toRegex(), ""))
                versionIsNew = serverNumber > currentNumber
            } catch (e: NumberFormatException) {
                OTAUtils.logError(e)
            }

        } else {
            try {
                val serverDate = format.parse(serverVersion)
                val currentDate = format.parse(localVersion)
                versionIsNew = serverDate.after(currentDate)
            } catch (e: ParseException) {
                OTAUtils.logError(e)
            }

        }

        return versionIsNew
    }

    fun extractVersionFrom(str: String, context: Context): String {
        var version = ""

        if (!str.isEmpty()) {
            var delimiter = OTAConfig.getInstance(context).delimiter
            val position = OTAConfig.getInstance(context).position

            if (delimiter.isEmpty()) {
                version = str
            } else {
                if (delimiter == ".") {
                    delimiter = Pattern.quote(".")
                }
                val tokens = str.split(delimiter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (position > -1 && position < tokens.size) {
                    version = tokens[position]
                }
            }
        }

        return version
    }
}
