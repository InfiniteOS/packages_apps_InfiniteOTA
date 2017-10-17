/*
 * Copyright (C) 2015 Chandra Poerwanto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ota.os.infinite.infiniteota.xml

import android.util.Xml

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException

import java.io.IOException
import java.io.InputStream

@Suppress("NAME_SHADOWING")
class OTAParser private constructor() {

    private var mDeviceName: String? = null
    private var mReleaseType: String? = null
    private var mDevice: OTADevice? = null

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(`in`: InputStream, deviceName: String, releaseType: String): OTADevice? {
        this.mDeviceName = deviceName
        this.mReleaseType = releaseType

        `in`.use { `in` ->
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`in`, null)
            parser.nextTag()
            readBuildType(parser)
            return mDevice
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBuildType(parser: XmlPullParser) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name.equals(mReleaseType!!, ignoreCase = true)) {
                readStable(parser)
            } else {
                skip(parser)
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readStable(parser: XmlPullParser) {
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name.equals(mDeviceName!!, ignoreCase = true)) {
                readDevice(parser)
            } else {
                skip(parser)
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDevice(parser: XmlPullParser) {
        parser.require(XmlPullParser.START_TAG, ns, mDeviceName)
        mDevice = OTADevice()
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val tagName = parser.name
            when {
                tagName.equals(FILENAME_TAG, ignoreCase = true) -> {
                    val tagValue = readTag(parser, tagName)
                    mDevice!!.latestVersion = tagValue
                }
                isUrlTag(tagName) -> {
                    val link = readLink(parser, tagName)
                    mDevice!!.addLink(link)
                }
                else -> skip(parser)
            }
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readLink(parser: XmlPullParser, tag: String): OTALink {
        parser.require(XmlPullParser.START_TAG, ns, tag)

        var id: String? = parser.getAttributeValue(null, ID)
        if (id == null || id.isEmpty()) {
            id = tag
        }
        val link = OTALink(id)
        val title = parser.getAttributeValue(null, TITLE)
        link.title = title
        val description = parser.getAttributeValue(null, DESCRIPTION)
        link.description = description
        val url = readText(parser)
        link.url = url

        parser.require(XmlPullParser.END_TAG, ns, tag)
        return link
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val text = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return text
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    companion object {

        private val ns: String? = null
        private val FILENAME_TAG = "Filename"
        private val URL_TAG = "Url"

        val ID = "id"
        val TITLE = "title"
        val DESCRIPTION = "description"
        val URL = "url"

        private var mInstance: OTAParser? = null

        val instance: OTAParser
            get() {
                if (mInstance == null) {
                    mInstance = OTAParser()
                }
                return mInstance as OTAParser
            }

        private fun isUrlTag(tagName: String): Boolean {
            return tagName.toLowerCase().endsWith(URL_TAG.toLowerCase())
        }
    }
}
