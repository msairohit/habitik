package com.habitik.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.habitik.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val PoppinsFont = FontFamily(
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = provider, weight = FontWeight.SemiBold)
)

val NunitoFont = FontFamily(
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider),
    Font(googleFont = GoogleFont("Nunito"), fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    displayMedium = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PoppinsFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = NunitoFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)
