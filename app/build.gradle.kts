import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.emathias.periodic"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.emathias.periodic"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Build config fields for AWS configuration
        // Read AWS credentials from local.properties (which is gitignored)
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        // AWS configuration with fallback to empty strings (will cause runtime error if not set)
        val awsRegion = localProperties.getProperty("aws.region", "")
        val awsAccessKeyId = localProperties.getProperty("aws.access_key_id", "")
        val awsSecretAccessKey = localProperties.getProperty("aws.secret_access_key", "")

        buildConfigField("String", "AWS_REGION", "\"$awsRegion\"")
        buildConfigField("String", "AWS_ACCESS_KEY_ID", "\"$awsAccessKeyId\"")
        buildConfigField("String", "AWS_SECRET_ACCESS_KEY", "\"$awsSecretAccessKey\"")
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/io.netty.versions.properties"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/LGPL2.1"
            excludes += "META-INF/AL2.0"
            excludes += "META-INF/LGPL2.1"
        }
    }
}

dependencies {
    ksp(libs.room.compiler)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.skedule)
    implementation(libs.work)
    implementation(libs.hilt)
    implementation(libs.androidx.hilt.work)

    // AWS Bedrock dependencies
    implementation(libs.aws.bedrock.runtime) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.aws.core) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.aws.auth) {
        exclude(group = "org.apache.httpcomponents", module = "httpclient")
        exclude(group = "org.apache.httpcomponents", module = "httpcore")
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation(libs.aws.url.connection.client)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
