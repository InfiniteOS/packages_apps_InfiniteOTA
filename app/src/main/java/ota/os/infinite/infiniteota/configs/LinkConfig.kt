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

package ota.os.infinite.infiniteota.configs

import android.content.Context

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ota.os.infinite.infiniteota.utils.OTAUtils
import ota.os.infinite.infiniteota.xml.OTALink
import ota.os.infinite.infiniteota.xml.OTAParser

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList
class LinkConfig private constructor() {
    private var mLinks: MutableList<OTALink>? = null

    fun getLinks(context: Context, force: Boolean): MutableList<OTALink>? {
        if (mLinks == null || force) {
            try {
                mLinks = ArrayList()

                val fis = context.openFileInput(FILENAME)
                val reader = BufferedReader(InputStreamReader(fis))
                val out = StringBuffer()
                var line: String
                while (true) {
                    line = reader.readLine() ?: break
                    out.append(line)
                }
                reader.close()
                fis.close()

                val jsonLinks = JSONArray(out.toString())
                for (i in 0 until jsonLinks.length()) {
                    val jsonLink = jsonLinks.getJSONObject(i)
                    val link = OTALink(jsonLink.getString(OTAParser.ID))
                    link.title=(jsonLink.getString(OTAParser.TITLE))
                    link.description=(jsonLink.getString(OTAParser.DESCRIPTION))
                    link.url=(jsonLink.getString(OTAParser.URL))
                    (mLinks as ArrayList<OTALink>).add(link)
                }
            } catch (e: JSONException) {
                OTAUtils.logError(e)
            } catch (e: IOException) {
                OTAUtils.logError(e)
            }

        }
        return mLinks
    }

    fun findLink(linkId: String, context: Context): OTALink? {
        val links = getLinks(context, false)
        links?.filter { it.id.equals(linkId,true) }?.forEach { return it }
        return null
    }

    interface LinkConfigListener {
        fun onConfigChange()
    }

    companion object {

        private val FILENAME = "links_conf"

        private var mInstance: LinkConfig? = null

        val instance: LinkConfig
            get() {
                if (mInstance == null) {
                    mInstance = LinkConfig()
                }
                return mInstance as LinkConfig
            }

        fun persistLinks(links: List<OTALink>, context: Context) {
            try {
                val dir = context.filesDir
                val file = File(dir, FILENAME)
                if (file.exists()) {
                    file.delete()
                }

                val fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE)

                val jsonLinks = JSONArray()
                for (link in links) {
                    val jsonLink = JSONObject()
                    jsonLink.put(OTAParser.ID, link.id)
                    jsonLink.put(OTAParser.TITLE, link.title)
                    jsonLink.put(OTAParser.DESCRIPTION, link.description)
                    jsonLink.put(OTAParser.URL, link.url)
                    jsonLinks.put(jsonLink)
                }

                fos.write(jsonLinks.toString().toByteArray())
                fos.close()

                val listener = getLinkConfigListener(context)
                listener?.onConfigChange()
            } catch (e: IOException) {
                OTAUtils.logError(e)
            } catch (e: JSONException) {
                OTAUtils.logError(e)
            }

        }

        private fun getLinkConfigListener(context: Context): LinkConfigListener? {
            return if (context is LinkConfigListener) {
                context
            } else null
        }
    }
}
