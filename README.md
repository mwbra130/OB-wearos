# OpenBubbles Wear

Wear OS companion for OpenBubbles. This repository builds the watch app and its shared protocol only; it does not build or patch a phone APK. The phone-side Morphe patch lives in the [mrcool-patches repository](https://github.com/MrCool-888/mrcool-patches).


The watch package identity is `com.openbubbles.messaging`. Keep it and its release signing key aligned with the phone patch so the Wear Data Layer can authenticate the companion.

## Message actions

With a matching phone bridge build, hold an iMessage bubble for quick reactions or Reply. Your reactions are highlighted; select the same reaction again to remove it. Swipe an incoming bubble left or an outgoing bubble right to reply. Replies show their quoted original and reply count.

In watch Settings, use **Swipe to reply** to choose between swiping toward the edge or the center. The choice applies immediately.

The bottom **Send message** button starts an ordinary message. Reaction notifications follow the existing phone and watch notification settings.

## APK FILE

Grab the latest pre-compiled application files from the **[Releases](../../releases)** page.

OpenBubbles Wear is an independent project and is not affiliated with OpenBubbles.

## License

OpenBubbles Wear is licensed under the [GNU General Public License v3.0](LICENSE). Third-party files retain their existing license notices.
