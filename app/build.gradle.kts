plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    id("org.jetbrains.kotlin.kapt") // Assim!
    id("com.google.devtools.ksp") // Adicione o plugin KSP

}

android {
    namespace = "com.yagosouza.crudcomposeapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yagosouza.crudcomposeapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.androidx.navigation.compose) // Navegação com Compose

    // Room (Banco de Dados Local)
    implementation(libs.androidx.room.runtime) // Componente de runtime do Room
//    kapt(libs.androidx.room.compiler) // Compilador de anotações do Room (processa DAOs, Entities, etc.)
    ksp(libs.androidx.room.compiler) // Compilador de anotações do Room (processa DAOs, Entities, etc.)
    implementation(libs.androidx.room.ktx) // Extensões Kotlin para Room (suporte a Coroutines e Flow)

    // Retrofit & Gson (Networking e Parsing JSON)
    implementation(libs.retrofit) // Cliente HTTP para Android e Java
    implementation(libs.converter.gson) // Conversor Gson para Retrofit (serializar/desserializar JSON)
    implementation(libs.logging.interceptor) // Interceptor para logs de requisições HTTP com OkHttp

    // Koin (Injeção de Dependência)
    implementation(libs.koin.android) // Koin para Android
    implementation(libs.koin.androidx.compose) // Integração do Koin com Jetpack Compose

}