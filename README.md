# xdsrun (Android)

Android client for logging into the Xidian University Srun campus network.

- Uses the core network login library from the upstream project. See the bundled core library documentation: [xdsrun-login/README.md](https://github.com/chitao1234/xdsrun-login/blob/main/README.md).
- Upstream core library: [NanCunChild/xdsrun-login]
- Android downstream fork: [chitao1234/xdsrun-login]

## Features

- Multi-account management
- Login status check
- Auto login on app start

## How it works

This app vendors and integrates the core login logic from the upstream `xdsrun-login` project, which implements the campus Web authentication flow (including the custom XXTEA-based `info` field handling). For protocol and implementation details, please refer to the core library README.

## Build and run

1. Open this `android` project in Android Studio.
2. Build and run on a connected device or emulator.
3. On first launch:
   - Add one or more campus accounts.
   - Optionally set a default account.
   - Enable “Auto login on app start” if desired.
4. Use the “Status” action to check current online status at any time.

## Project links

- Upstream core library (Go): [NanCunChild/xdsrun-login]
- Android downstream fork: [chitao1234/xdsrun-login]

## Credits

Original authentication research and core CLI/library by NanCunChild. Thank you for making the upstream `xdsrun-login` possible.

## License

This project is licensed under the GNU General Public License v3.0 (GPLv3).
See the [LICENSE](./LICENSE) file for details.

[chitao1234/xdsrun-login]: https://github.com/chitao1234/xdsrun-login
[NanCunChild/xdsrun-login]: https://github.com/NanCunChild/xdsrun-login