package de.kruemelopment.org.bewerber

import android.Manifest
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.util.Objects
import java.util.Random

class MainAcitivity : AppCompatActivity() {
    private var myDB: DataBaseHelper? = null
    private var fileStorePath: String? = null
    private val oldInputWords: MutableList<String> = ArrayList()
    private val additionalWords: MutableList<String> = ArrayList()
    private var textInputEditText: TextInputEditText? = null
    private var sloganTextView: TextView? = null
    private var myToast: MyToast? = null
    private var saveSlogan: FloatingActionButton? = null
    private var shareSlogan: FloatingActionButton? = null
    private var newSlogan: FloatingActionButton? = null
    private var nightmode = false
    private var additionalWordsLauncher: ActivityResultLauncher<Intent>? = null
    private var oldInputWordsLauncher: ActivityResultLauncher<Intent>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val sp2 = getSharedPreferences("settings", 0)
            nightmode = sp2.getBoolean("mode", false)
            if (nightmode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)
        myDB = DataBaseHelper(this)
        fileStorePath = Environment.DIRECTORY_PICTURES + File.separator + "indviduelle Werbesprüche"
        myToast = MyToast(this)
        agbs()
        start()
        additionalWordsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                loadAdditionalWords()
            }
        }
        oldInputWordsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                loadOldInputWords()
            }
        }
    }

    private fun agbs() {
        val sese = getSharedPreferences("Start", 0)
        val web = sese.getBoolean("agbs", false)
        if (!web) {
            val dialol = Dialog(this, R.style.AppDialog)
            dialol.setContentView(R.layout.webdialog)
            val ja = dialol.findViewById<TextView>(R.id.textView55)
            val nein = dialol.findViewById<TextView>(R.id.textView88)
            ja.setOnClickListener {
                val sp8 = getSharedPreferences("Start", 0)
                val ede = sp8.edit()
                ede.putBoolean("agbs", true)
                ede.apply()
                dialol.dismiss()
            }
            nein.setOnClickListener { finishAndRemoveTask() }
            val textView = dialol.findViewById<TextView>(R.id.textView44)
            textView.text = Html.fromHtml(
                "Mit der Nutzung dieser App aktzeptiere ich die " +
                        "<a href=\"https://www.kruemelopment-dev.de/datenschutzerklaerung\">Datenschutzerklärung</a>" + " und die " + "<a href=\"https://www.kruemelopment-dev.de/nutzungsbedingungen\">Nutzungsbedingungen</a>" + " von Krümelopment Dev",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            textView.movementMethod = LinkMovementMethod.getInstance()
            dialol.setCancelable(false)
            dialol.show()
        }
    }

    private fun start() {
        setContentView(R.layout.activity_main)
        loadOldInputWords()
        loadAdditionalWords()
        textInputEditText = findViewById(R.id.textInputEdittext)
        textInputEditText?.setSingleLine()
        textInputEditText?.imeOptions = EditorInfo.IME_ACTION_DONE
        saveSlogan = findViewById(R.id.save)
        shareSlogan = findViewById(R.id.share)
        newSlogan = findViewById(R.id.refresh)
        val saveInput = findViewById<FloatingActionButton>(R.id.saveinput)
        sloganTextView = findViewById(R.id.sloganTextView)
        saveSlogan?.setOnClickListener {
            if (sloganTextView?.text.toString() != "") {
                spruchspeichern(sloganTextView?.text.toString())
            } else myToast!!.showError("Lass dir erstmal einen Spruch erstellen, bevor du ihn speichern willst!")
        }
        shareSlogan?.setOnClickListener {
            if (sloganTextView?.text.toString() != "") {
                saveSlogan?.visibility = View.GONE
                shareSlogan?.visibility = View.GONE
                newSlogan?.visibility = View.GONE
                val mainView = window.decorView.findViewById<View>(R.id.wrapper)
                val bitmap = getBitmapFromView(mainView)
                saveSlogan?.visibility = View.VISIBLE
                shareSlogan?.visibility = View.VISIBLE
                newSlogan?.visibility = View.VISIBLE
                val imagepath =
                    File(getExternalFilesDir(null), sloganTextView?.text.toString() + ".png")
                try {
                    if (!imagepath.createNewFile()) {
                        myToast!!.showError("Teilen fehlgeschlagen!")
                        return@setOnClickListener
                    }
                    val fil = FileOutputStream(imagepath)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fil)
                    fil.flush()
                    fil.close()
                    val uri = FileProvider.getUriForFile(
                        this@MainAcitivity,
                        "de.kruemelopment.org.bewerber.provider",
                        imagepath
                    )
                    val sharingintent = Intent(Intent.ACTION_SEND)
                    sharingintent.setType("image/*")
                    sharingintent.putExtra(Intent.EXTRA_STREAM, uri)
                    startActivity(Intent.createChooser(sharingintent, "Teilen über..."))
                } catch (e: IOException) {
                    myToast!!.showError("Teilen fehlgeschlagen!")
                }
            } else {
                myToast!!.showError("Lass dir erstmal einen Werbespruch erstellen, bevor du ihn teilen willst!")
            }
        }
        newSlogan?.setOnClickListener { createAndSetSpruch() }
        textInputEditText?.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                createAndSetSpruch()
            }
            false
        }
        saveInput.setOnClickListener {
            if (checkInput(textInputEditText)) {
                saveInputWordToDataBase(
                    textInputEditText?.text.toString()
                )
            } else {
                myToast!!.showError("Gib erstmal ein Wort ein, bevor du es speichern möchtest!")
            }
        }
        handleintentfromshortcut(intent)
    }

    private fun loadOldInputWords() {
        oldInputWords.clear()
        val res = myDB!!.getAllData("worter")
        if (res.count > 0) {
            while (res.moveToNext()) {
                oldInputWords.add(res.getString(1))
            }
        }
        if (oldInputWords.isEmpty()) {
            oldInputWords.addAll(mutableListOf("Cola", "Käse", "Krümelopment Dev"))
            myDB!!.insertData("Cola", "worter")
            myDB!!.insertData("Käse", "worter")
            myDB!!.insertData("Krümelopment Dev", "worter")
        }
    }

    private fun loadAdditionalWords() {
        additionalWords.clear()
        val res = myDB!!.getAllData("erganzung")
        if (res.count > 0) {
            while (res.moveToNext()) {
                additionalWords.add(res.getString(1))
            }
        }
        if (additionalWords.isEmpty()) {
            additionalWords.addAll(
                mutableListOf(
                    "Cola",
                    "Käse",
                    "Bier",
                    "Salami",
                    "Fanta",
                    "Sprite",
                    "Wasser",
                    "Döner",
                    "Rosen"
                )
            )
            myDB!!.insertData("Cola", "erganzung")
            myDB!!.insertData("Käse", "erganzung")
            myDB!!.insertData("Bier", "erganzung")
            myDB!!.insertData("Salami", "erganzung")
            myDB!!.insertData("Fanta", "erganzung")
            myDB!!.insertData("Sprite", "erganzung")
            myDB!!.insertData("Wasser", "erganzung")
            myDB!!.insertData("Döner", "erganzung")
            myDB!!.insertData("Rosen", "erganzung")
        }
    }

    private fun saveInputWordToDataBase(word: String) {
        var found = false
        val res = myDB!!.getAllData("worter")
        if (res.count > 0) {
            while (res.moveToNext()) {
                if (res.getString(1) == word) {
                    found = true
                    break
                }
            }
        }
        if (!found) {
            myDB!!.insertData(word, "worter")
            myToast!!.showSuccess("Gespeichert")
            oldInputWords.add(word)
        } else {
            myToast!!.showError("Dieses Wort wurde schon gespeichert")
        }
    }

    private fun saveAdditionalWord(word: String) {
        var found = false
        val res = myDB!!.getAllData("erganzung")
        if (res.count > 0) {
            while (res.moveToNext()) {
                if (res.getString(1) == word) {
                    found = true
                    break
                }
            }
        }
        if (!found) {
            myDB!!.insertData(word, "erganzung")
            additionalWords.add(word)
        }
    }

    private fun checkInput(editText: TextInputEditText?): Boolean {
        return editText!!.text != null && editText.text.toString().trim { it <= ' ' }.isNotEmpty()
    }

    private fun getSpruch(word: String): String {
        val sprueche = resources.getStringArray(R.array.sprueche)
        var spruch = sprueche[Random().nextInt(sprueche.size)]
        spruch = spruch.replace("_", word)
        if (spruch.contains("~")) {
            val str = additionalWords[Random().nextInt(additionalWords.size)]
            spruch = spruch.replace("~", str)
        }
        if (spruch.contains("#")) {
            val st = additionalWords[Random().nextInt(additionalWords.size)]
            spruch = spruch.replace("#", st)
        }
        if (spruch.contains("\n")) {
            val kl = spruch.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            spruch = """
                ${kl[0]}
                ${kl[1]}
                """.trimIndent()
        }
        return spruch
    }

    private fun spruchspeichern(spruchalt: String) {
        saveSlogan!!.visibility = View.GONE
        shareSlogan!!.visibility = View.GONE
        newSlogan!!.visibility = View.GONE
        val mainView = window.decorView.findViewById<View>(R.id.wrapper)
        val bitmap = getBitmapFromView(mainView)
        saveSlogan!!.visibility = View.VISIBLE
        shareSlogan!!.visibility = View.VISIBLE
        newSlogan!!.visibility = View.VISIBLE
        try {
            saveImage(bitmap, spruchalt)
        } catch (e: IOException) {
            myToast!!.showError("Spruch konnte nicht gespeichert werden!")
        }
    }

    @Throws(IOException::class)
    private fun saveImage(bitmap: Bitmap, name: String) {
        val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.png")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, fileStorePath)
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            resolver.openOutputStream(imageUri!!)
        } else {
            val result = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            if (result != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                return
            }
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + File.separator + "indviduelle Werbesprüche"
            val parent = File(imagesDir)
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    myToast!!.showError("Spruch konnte nicht gespeichert werden!")
                    return
                }
            }
            val image = File(imagesDir, "$name.png")
            Files.newOutputStream(image.toPath())
        }
        if (fos != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaScannerConnection.scanFile(
                this,
                arrayOf(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + File.separator + "indviduelle Werbesprüche" + File.separator + name + ".png"
                ),
                null,
                null
            )
        }
        myToast!!.showSuccess("Spruch wurde gespeichert!")
    }

    private fun handleintentfromshortcut(intent: Intent) {
        try {
            val extra = intent.getStringExtra("wort")
            if (!extra.isNullOrEmpty()) {
                textInputEditText!!.setText(extra)
                createAndSetSpruch()
                intent.removeExtra("wort")
            }
        } catch (ignored: Exception) {
        }
    }

    private fun createAndSetSpruch() {
        if (checkInput(textInputEditText)) {
            val edittextText = Objects.requireNonNull(
                textInputEditText!!.text
            ).toString()
            val spruch = getSpruch(edittextText)
            sloganTextView!!.text = spruch
            if (!additionalWords.contains(edittextText)) saveAdditionalWord(edittextText)
        } else {
            myToast!!.showError("Gib erstmal ein Wort ein, bevor du einen Spruch speichern willst!")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.dots, menu)
        val darkmode = menu.findItem(R.id.nightmode)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            darkmode.setVisible(false)
        } else {
            darkmode.setChecked(nightmode)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item3) {
            openContact()
            return true
        } else if (item.itemId == R.id.item4) {
            openTermsofUse()
            return true
        } else if (item.itemId == R.id.item5) {
            openPrivacyPolicy()
            return true
        } else if (item.itemId == R.id.item7) {
            startActivity(Intent(this, Infos::class.java))
            return true
        } else if (item.itemId == R.id.item9) {
            val intent = Intent(this, WordManagement::class.java)
            intent.putExtra("type", "erganzung")
            additionalWordsLauncher!!.launch(intent)
            return true
        } else if (item.itemId == R.id.item11) {
            val intent = Intent(this, WordManagement::class.java)
            intent.putExtra("type", "worter")
            oldInputWordsLauncher!!.launch(intent)
            return true
        } else if (item.itemId == R.id.nightmode) {
            nightmode = !nightmode
            val sp = getSharedPreferences("settings", 0)
            val e = sp.edit()
            e.putBoolean("mode", nightmode)
            e.apply()
            if (nightmode) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) else AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openContact() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setData(Uri.parse("mailto:kontakt@kruemelopment-dev.de"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "individuelle Werbung")
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(emailIntent)
    }

    private fun openTermsofUse() {
        val uri = Uri.parse("https://www.kruemelopment-dev.de/nutzungsbedingungen")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun openPrivacyPolicy() {
        val uri = Uri.parse("https://www.kruemelopment-dev.de/datenschutzerklärung")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var i = 0
        val len = permissions.size
        while (i < len) {
            val permission = permissions[i]
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                val showRationale = shouldShowRequestPermissionRationale(permission)
                if (!showRationale) {
                    myToast!!.showError("Du hast uns dauerhaft das Recht entzogen nach der Speicherberechtigung zu fragen, ohne Speicherberechtigung kann ich nichts speichern!")
                }
            }
            i++
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val sammel: MutableList<String> = ArrayList()
        val res = myDB!!.getAllData("worter")
        if (res.count > 0) {
            while (res.moveToNext()) {
                sammel.add(res.getString(1))
            }
        }
        val drawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher_round) ?: return
        val bmp = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        val icon = Icon.createWithBitmap(bmp)
        var shortcut: ShortcutInfo? = null
        var shortcut2: ShortcutInfo? = null
        val shortcut3: ShortcutInfo
        val shortcutManager = getSystemService(
            ShortcutManager::class.java
        )
        when (sammel.size) {
            2 -> {
                val intent2 = Intent(this, MainAcitivity::class.java)
                intent2.putExtra("wort", sammel[sammel.size - 2])
                intent2.setAction(Intent.ACTION_MAIN)
                shortcut2 = ShortcutInfo.Builder(this, "id2")
                    .setShortLabel(sammel[sammel.size - 2])
                    .setLongLabel(sammel[sammel.size - 2])
                    .setIcon(icon)
                    .setIntent(intent2)
                    .build()
                val intent3 = Intent(this, MainAcitivity::class.java)
                intent3.putExtra("wort", sammel[sammel.size - 1])
                intent3.setAction(Intent.ACTION_MAIN)
                shortcut3 = ShortcutInfo.Builder(this, "id3")
                    .setShortLabel(sammel[sammel.size - 1])
                    .setLongLabel(sammel[sammel.size - 1])
                    .setIcon(icon)
                    .setIntent(intent3)
                    .build()
            }

            1 -> {
                val intent3 = Intent(this, MainAcitivity::class.java)
                intent3.putExtra("wort", sammel[sammel.size - 1])
                intent3.setAction(Intent.ACTION_MAIN)
                shortcut3 = ShortcutInfo.Builder(this, "id3")
                    .setShortLabel(sammel[sammel.size - 1])
                    .setLongLabel(sammel[sammel.size - 1])
                    .setIcon(icon)
                    .setIntent(intent3)
                    .build()
            }

            else -> {
                val intent = Intent(this, MainAcitivity::class.java)
                intent.putExtra("wort", sammel[sammel.size - 3])
                intent.setAction(Intent.ACTION_MAIN)
                shortcut = ShortcutInfo.Builder(this, "id1")
                    .setShortLabel(sammel[sammel.size - 3])
                    .setLongLabel(sammel[sammel.size - 3])
                    .setIcon(icon)
                    .setIntent(intent)
                    .build()
                val intent2 = Intent(this, MainAcitivity::class.java)
                intent2.putExtra("wort", sammel[sammel.size - 2])
                intent2.setAction(Intent.ACTION_MAIN)
                shortcut2 = ShortcutInfo.Builder(this, "id2")
                    .setShortLabel(sammel[sammel.size - 2])
                    .setLongLabel(sammel[sammel.size - 2])
                    .setIcon(icon)
                    .setIntent(intent2)
                    .build()
                val intent3 = Intent(this, MainAcitivity::class.java)
                intent3.putExtra("wort", sammel[sammel.size - 1])
                intent3.setAction(Intent.ACTION_MAIN)
                shortcut3 = ShortcutInfo.Builder(this, "id3")
                    .setShortLabel(sammel[sammel.size - 1])
                    .setLongLabel(sammel[sammel.size - 1])
                    .setIcon(icon)
                    .setIntent(intent3)
                    .build()
            }
        }
        assert(shortcutManager != null)
        if (shortcut != null) shortcutManager!!.setDynamicShortcuts(
            listOf(
                shortcut,
                shortcut2,
                shortcut3
            )
        ) else if (shortcut2 != null) shortcutManager!!.setDynamicShortcuts(
            listOf(shortcut2, shortcut3)
        ) else shortcutManager!!.setDynamicShortcuts(
            listOf(shortcut3)
        )
    }
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
}
