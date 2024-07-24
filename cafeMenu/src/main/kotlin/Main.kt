package org.example

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate
import java.util.Scanner

// If I hypothetically wanted to send it as an email
/*
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
*/

fun main() {
    val url = "https://ramseysolutions.corporate-dining.com/dining-location/cafe/"
    val pdfUrl = findPdfUrl(url)
    if (pdfUrl != null) {
        val extractedText = extractFromPdf(pdfUrl).trimIndent()
        println(formatMenu(extractedText))

        // Supposedly sends an email
        /*
        val receiverEmail = "[RECEIVING EMAIL ADDRESS GOES HERE]"
        val subject = "PDF is now formatted"
        sendEmail(receiverEmail, subject, formatMenu(extractedText))
         */
    } else {
        println("PDF not found on the web")
    }
}

fun formatMenu(menuText: String): String {
    val sections = menuText.split("\n").map { it.trim() }
    val currentDay = LocalDate.now().dayOfWeek
    val day = currentDay.toString()

    val formattedSections = mutableListOf<String>()
    val remove = listOf("CLASSICS", "2,000", "needs", "TUESDAY")

    for (section in sections) {
        if (remove.any { section.startsWith(it) }) {
            continue
        }

        when {
            section.startsWith("MONDAY") -> {
                formattedSections.add("\n" + formatDayMenu(section))
            }
            section.startsWith("TACO") -> {
                formattedSections.add("\n" + formatTacoTuesday(section))
            }
            section.startsWith("WEDNESDAY") -> {
                formattedSections.add("\n" + formatDayMenu(section))
            }
            section.startsWith("THURSDAY") -> {
                formattedSections.add("\n" + formatDayMenu(section))
            }
            section.startsWith("FR") -> {
                formattedSections.add("\n" + "FRIDAY\n" + formatFriday(section.substring(8)))
            }
            section.startsWith("so ld", ignoreCase = true) -> {
                formattedSections.remove("so ld by weight")
                formattedSections.add("Sold by weight")
            }
            else -> {
                formattedSections.add(section)
            }
        }
    }

    return formattedSections.joinToString("\n")
}

fun formatDayMenu(dayMenu: String): String {
    val lines = dayMenu.lines().map { it.trim() }.filter { it.isNotEmpty() }
    return lines.joinToString()
}

fun formatTacoTuesday(tacoTuesdayMenu: String): String {
    val lines = tacoTuesdayMenu.lines().map { it.trim() }.filter { it.isNotEmpty() }
    return lines.joinToString(postfix = " TUESDAY")
}

fun formatFriday(fridayMenu: String): String = fridayMenu

fun findPdfUrl(webUrl: String): String? {
    try {
        val uri = URI(webUrl)
        val url = uri.toURL()
        val connection = url.openConnection() as HttpURLConnection
        connection.connect()

        val inputStream = BufferedInputStream(connection.inputStream)
        val scanner = Scanner(inputStream)
        var pdfUrl: String? = null

        while (scanner.hasNextLine()) {
            val line = scanner.nextLine().trim()
            if (line.contains(".pdf")) {
                pdfUrl = extractPdfUrl(line)
                break
            }
        }
        scanner.close()
        inputStream.close()
        connection.disconnect()
        return pdfUrl
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun extractPdfUrl(line: String): String? {
    val hRefPattern = "<a\\s+(?:[^>]*?\\s+)?href=([\"'])(.*?)\\1".toRegex()
    val match = hRefPattern.find(line)

    return match?.groupValues?.get(2)
}

fun extractFromPdf(url: String): String {
    val pdfUri = URI(url)
    val pdfUrl = pdfUri.toURL()
    val connection = pdfUrl.openConnection()
    val inputStream = BufferedInputStream(connection.getInputStream())

    val doc = PDDocument.load(inputStream)
    val stripper = PDFTextStripper()
    val text = stripper.getText(doc)

    doc.close()
    inputStream.close()

    return text
}

// Maybe works if I were to send an email??
/*
fun sendEmail(
    receiverEmail: String,
    subject: String,
    body: String,
) {
    val senderEmail = "[SENDER'S EMAIL]"
    val senderPassword = "[SENDER'S APP PASSWORD or PASSWORD]"

    val properties = Properties()
    properties["mail.smtp.auth"] = "true"
    properties["mail.smtp.starttls.enable"] = "true"
    properties["mail.smtp.host"] = "[SENDER'S SMTP HOST (like gmail, yahoo, etc.]"
    properties["mail.smtp.port"] = "587"

    val session =
        Session.getInstance(
            properties,
            object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication = PasswordAuthentication(senderEmail, senderPassword)
            },
        )

    try {
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(senderEmail))
        message.addRecipient(Message.RecipientType.TO, InternetAddress(receiverEmail))
        message.subject = subject
        message.setText(body)
        Transport.send(message)
        println("Sent email to $receiverEmail")
    } catch (e: MessagingException) {
        println("Failed to send. Error messgage: ${e.message}")
        e.printStackTrace()
    }
}
*/
