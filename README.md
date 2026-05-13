# Salus Widget

A circular Android home screen widget that randomly calls one of your selected contacts when long-pressed for 3 seconds.

## Features

- **Circular Widget**: A clean, circular green widget with a phone icon
- **Long-Press Activation**: Hold the widget for 3 seconds to trigger a random call
- **Contact Selection**: Configure which contacts to include via the widget configuration activity
- **Contact Search**: Easily search and filter contacts during configuration
- **Local Storage**: Selected contacts are stored locally using SharedPreferences
- **Permission Handling**: Gracefully handles READ_CONTACTS and CALL_PHONE permissions

## Project Structure

```
com.salus.widget/
├── config/
│   ├── WidgetConfigActivity.kt    # Contact selection configuration activity
│   └── ContactsAdapter.kt         # RecyclerView adapter for contact list
├── data/
│   ├── Contact.kt                 # Contact data model
│   └── ContactRepository.kt       # SharedPreferences-based contact storage
├── util/
│   └── ContactLoader.kt           # Utility for loading device contacts
└── widget/
    ├── SalusWidgetProvider.kt     # AppWidgetProvider implementation
    └── WidgetTouchActivity.kt     # Transparent activity for long-press detection
```

## Permissions

- `READ_CONTACTS` - Required to access and display contacts for selection
- `CALL_PHONE` - Required to initiate phone calls (falls back to dialer if denied)

## How It Works

1. **Add Widget**: Long-press on your home screen and add the Salus Call Widget
2. **Configure Contacts**: The configuration activity opens automatically, select contacts to include
3. **Use Widget**: Tap and hold the widget for 3 seconds to call a random contact from your selection

## Building

```bash
./gradlew assembleDebug
```

## Requirements

- Android SDK 26+ (Android 8.0 Oreo)
- Kotlin 1.9+
- Android Gradle Plugin 8.2.0

## License

See [LICENSE](LICENSE) file for details.
