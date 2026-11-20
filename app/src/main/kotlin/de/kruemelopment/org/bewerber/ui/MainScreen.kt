package de.kruemelopment.org.bewerber.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.kruemelopment.org.bewerber.BewerberViewModel
import de.kruemelopment.org.bewerber.ShareCardRenderer
import de.kruemelopment.org.bewerber.ui.sheets.SettingsInfoSheet
import de.kruemelopment.org.bewerber.ui.sheets.ShareBottomSheet
import de.kruemelopment.org.bewerber.ui.sheets.SloganCatalogSheet
import de.kruemelopment.org.bewerber.ui.sheets.WordsFavoritesSheet

@Composable
fun MainScreen(
    viewModel: BewerberViewModel,
    onSpeakSlogan: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showWordsSheet by rememberSaveable { mutableStateOf(false) }
    var wordsSheetInitialTab by rememberSaveable { mutableIntStateOf(0) }
    var showCatalogSheet by rememberSaveable { mutableStateOf(false) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }
    var showShareSheet by rememberSaveable { mutableStateOf(false) }

    var spinCount by rememberSaveable { mutableIntStateOf(0) }
    val diceRotation by animateFloatAsState(
        targetValue = spinCount * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "diceSpin"
    )

    val favScale by animateFloatAsState(
        targetValue = if (uiState.isCurrentSloganFavorite) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "favScale"
    )

    val bgColorArgb = MaterialTheme.colorScheme.surfaceContainerLow.toArgb()
    val cardColorArgb = MaterialTheme.colorScheme.surfaceContainerHigh.toArgb()
    val accentColorArgb = MaterialTheme.colorScheme.primary.toArgb()
    val textColorArgb = MaterialTheme.colorScheme.onSurface.toArgb()

    LaunchedEffect(uiState.statusMessage, uiState.lastUndoAction) {
        val msg = uiState.statusMessage
        if (!msg.isNullOrBlank()) {
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = if (uiState.lastUndoAction != null) "Rückgängig" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed && uiState.lastUndoAction != null) {
                viewModel.undoLastDelete()
            } else {
                viewModel.clearStatusMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sprücheklopfer",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            wordsSheetInitialTab = 0
                            showWordsSheet = true
                        }
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.favoriteSlogans.isNotEmpty()) {
                                    Badge {
                                        Text(uiState.favoriteSlogans.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmarks,
                                contentDescription = "Wörter & Favoriten"
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            showCatalogSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "Sprüche-Katalog & Eigene Vorlagen"
                        )
                    }
                    IconButton(
                        onClick = {
                            showSettingsSheet = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Palette,
                            contentDescription = "Einstellungen & Infos"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 88.dp)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(36.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            spinCount++
                            focusManager.clearFocus()
                            if (uiState.inputWord.isBlank() && uiState.savedWords.isNotEmpty()) {
                                viewModel.selectQuickWord(uiState.savedWords.random())
                            } else {
                                viewModel.generateSlogan()
                            }
                        },
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Casino,
                            contentDescription = "Neuer Spruch",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(diceRotation)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Neuer Spruch",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            ExpressiveHeroSloganCard(
                currentSlogan = uiState.currentSlogan,
                currentWord = uiState.inputWord,
                historySize = uiState.history.size,
                historyIndex = uiState.historyIndex,
                canGoBack = uiState.canGoBackInHistory,
                canGoForward = uiState.canGoForwardInHistory,
                isFavorite = uiState.isCurrentSloganFavorite,
                favScale = favScale,
                onNavigateBack = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.navigateHistoryBack()
                },
                onNavigateForward = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    viewModel.navigateHistoryForward()
                },
                onStartRandomSlogan = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    spinCount++
                    val randomWord = uiState.savedWords.randomOrNull() ?: "Kaffee"
                    viewModel.selectQuickWord(randomWord)
                },
                onToggleFavorite = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleCurrentSloganFavorite()
                },
                onSave = {
                    if (uiState.currentSlogan.isBlank()) {
                        viewModel.showStatus(
                            "Lass dir zuerst einen Spruch erstellen, bevor du ihn speicherst!",
                            true
                        )
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val bmp = ShareCardRenderer.renderShareCard(
                            slogan = uiState.currentSlogan,
                            word = uiState.inputWord,
                            backgroundColorArgb = bgColorArgb,
                            cardSurfaceColorArgb = cardColorArgb,
                            accentColorArgb = accentColorArgb,
                            textColorArgb = textColorArgb
                        )
                        val res = ShareCardRenderer.saveSloganImageToGallery(
                            context,
                            bmp,
                            uiState.currentSlogan
                        )
                        if (res.isSuccess) {
                            viewModel.showStatus("Spruch wurde als Poster in deiner Galerie gespeichert!", false)
                        } else {
                            viewModel.showStatus("Spruch konnte nicht gespeichert werden!", true)
                        }
                    }
                },
                onShare = {
                    if (uiState.currentSlogan.isBlank()) {
                        viewModel.showStatus(
                            "Lass dir zuerst einen Werbespruch erstellen, bevor du ihn teilst!",
                            true
                        )
                    } else {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        showShareSheet = true
                    }
                },
                onSpeak = {
                    if (uiState.currentSlogan.isNotBlank()) {
                        onSpeakSlogan(uiState.currentSlogan)
                    }
                },
                onCopy = {
                    if (uiState.currentSlogan.isNotBlank()) {
                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("Werbespruch", uiState.currentSlogan)
                        )
                        viewModel.showStatus("Spruch in die Zwischenablage kopiert!", false)
                    }
                }
            )

            OutlinedTextField(
                value = uiState.inputWord,
                onValueChange = viewModel::onInputWordChanged,
                label = { Text("Begriff für deine Werbung") },
                placeholder = { Text("z. B. Cola, Käse, Feierabend...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.inputWord.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onInputWordChanged("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Eingabe löschen"
                                )
                            }
                        }
                        FilledTonalIconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.saveCurrentInputWord()
                            },
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BookmarkAdd,
                                contentDescription = "Wort speichern"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        spinCount++
                        viewModel.generateSlogan()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gespeicherte Wörter (1-Tap):",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            wordsSheetInitialTab = 1
                            showWordsSheet = true
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Verwalten")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(
                        items = uiState.savedWords,
                        key = { index, word -> "${word.hashCode()}_$index" }
                    ) { _, word ->
                        val isSelected = uiState.inputWord.trim().equals(word, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                focusManager.clearFocus()
                                spinCount++
                                viewModel.selectQuickWord(word)
                            },
                            label = {
                                Text(
                                    text = word,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showWordsSheet) {
        WordsFavoritesSheet(
            uiState = uiState,
            initialTab = wordsSheetInitialTab,
            onDismiss = { showWordsSheet = false },
            onSelectFavoriteSlogan = viewModel::selectFavoriteSlogan,
            onRemoveFavoriteSlogan = viewModel::removeFavoriteSlogan,
            onSelectQuickWord = { word ->
                spinCount++
                viewModel.selectQuickWord(word)
            },
            onAddSavedWord = viewModel::addSavedWord,
            onDeleteSavedWord = viewModel::deleteSavedWord,
            onAddAdditionalWord = viewModel::addAdditionalWord,
            onDeleteAdditionalWord = viewModel::deleteAdditionalWord,
            onSpeakSlogan = onSpeakSlogan,
            onStatusMessage = viewModel::showStatus
        )
    }

    if (showCatalogSheet) {
        SloganCatalogSheet(
            uiState = uiState,
            onDismiss = { showCatalogSheet = false },
            onAddCustomTemplate = viewModel::addCustomTemplate,
            onDeleteCustomTemplate = viewModel::deleteCustomTemplate,
            onApplyTemplate = { template ->
                spinCount++
                val wordToUse = uiState.inputWord.ifBlank {
                    uiState.savedWords.firstOrNull() ?: "Cola"
                }
                viewModel.generateSlogan(wordOverride = wordToUse, specificTemplate = template)
            }
        )
    }

    if (showSettingsSheet) {
        SettingsInfoSheet(
            uiState = uiState,
            onDismiss = { showSettingsSheet = false },
            onSetThemeMode = viewModel::setThemeMode,
            onSetUseDynamicColor = viewModel::setUseDynamicColor
        )
    }

    if (showShareSheet && uiState.currentSlogan.isNotBlank()) {
        ShareBottomSheet(
            slogan = uiState.currentSlogan,
            word = uiState.inputWord,
            onDismiss = { showShareSheet = false },
            onStatusMessage = viewModel::showStatus
        )
    }
}

@Composable
private fun ExpressiveHeroSloganCard(
    currentSlogan: String,
    currentWord: String,
    historySize: Int,
    historyIndex: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    isFavorite: Boolean,
    favScale: Float,
    onNavigateBack: () -> Unit,
    onNavigateForward: () -> Unit,
    onStartRandomSlogan: () -> Unit,
    onToggleFavorite: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    val accentColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    var dragAccumulated by remember { mutableFloatStateOf(0f) }

    ElevatedCard(
        shape = RoundedCornerShape(36.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(36.dp))
            .pointerInput(canGoBack, canGoForward) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulated = 0f },
                    onDragEnd = {
                        if (dragAccumulated > 90f && canGoBack) {
                            onNavigateBack()
                        } else if (dragAccumulated < -90f && canGoForward) {
                            onNavigateForward()
                        }
                        dragAccumulated = 0f
                    }
                ) { _, dragAmount ->
                    dragAccumulated += dragAmount
                }
            }
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
            ) {
                drawCircle(
                    color = accentColor.copy(alpha = 0.08f),
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.9f, size.height * 0.12f)
                )
                drawCircle(
                    color = tertiaryColor.copy(alpha = 0.06f),
                    radius = size.minDimension * 0.36f,
                    center = Offset(size.width * 0.08f, size.height * 0.88f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentWord.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "#${currentWord.trim()}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (historySize > 0) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                IconButton(
                                    onClick = onNavigateBack,
                                    enabled = canGoBack,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                        contentDescription = "Vorheriger Spruch"
                                    )
                                }
                                Text(
                                    text = "${historyIndex + 1} / $historySize",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                IconButton(
                                    onClick = onNavigateForward,
                                    enabled = canGoForward,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = "Nächster Spruch"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentSlogan,
                    transitionSpec = {
                        (scaleIn(
                            initialScale = 0.86f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        ) + fadeIn()).togetherWith(
                            scaleOut(targetScale = 0.92f) + fadeOut()
                        )
                    },
                    label = "heroSloganAnimatedContent",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) { slogan ->
                    if (slogan.isBlank()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(68.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Campaign,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Bereit für deinen Werbespruch!",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Gib unten ein Wort ein oder tippe auf einen Quick-Chip!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            FilledTonalButton(
                                onClick = onStartRandomSlogan,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Casino,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Zufalls-Spruch starten")
                            }
                        }
                    } else {
                        val dynamicTextStyle = when {
                            slogan.length <= 42 -> MaterialTheme.typography.displaySmall
                            slogan.length <= 85 -> MaterialTheme.typography.headlineMedium
                            else -> MaterialTheme.typography.headlineSmall
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "„",
                                fontSize = 48.sp,
                                lineHeight = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                            )
                            Text(
                                text = slogan,
                                style = dynamicTextStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                if (currentSlogan.isNotBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalIconToggleButton(
                            checked = isFavorite,
                            onCheckedChange = { onToggleFavorite() },
                            modifier = Modifier.scale(favScale)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = if (isFavorite) "Aus Favoriten entfernen" else "Als Favorit merken",
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalIconButton(onClick = onSave) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = "Spruch als Poster in Galerie speichern"
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalIconButton(onClick = onShare) {
                            Icon(
                                imageVector = Icons.Rounded.Share,
                                contentDescription = "Spruch teilen"
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalIconButton(onClick = onSpeak) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Spruch vorlesen"
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledTonalIconButton(onClick = onCopy) {
                            Icon(
                                imageVector = Icons.Rounded.ContentCopy,
                                contentDescription = "Spruch kopieren"
                            )
                        }
                    }
                }
            }
        }
    }
}
