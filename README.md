# LexonRTP

High performance Random Teleport plugin with 1v1 matchmaking, Redis cross-server support & SQLite persistence for Paper & Folia.

## Features

- **Solo RTP** — Teleport to a random safe location with `/rtp`
- **1v1 Matchmaking** — Queue up and fight with `/rtpqueue`
- **Folia & Paper Support** — Single codebase runs on both platforms
- **Redis Cross-Server Support** — Optional cross-server RTP via Redis + BungeeCord messaging (e.g. RTP from lobby to main world). Requires Redis. Disabled by default.
- **Local Storage Fallback** — No external database required when Redis is disabled
- **Async Chunk Loading** — Zero TPS impact, chunks load asynchronously
- **GUI Menus** — Fully configurable solo and queue menus
- **Safe Location Finder** — Avoids lava, water, cactus and other dangerous blocks
- **Countdown System** — Cancelable countdown on movement for solo mode
- **Multi-world** — Overworld, Nether, End with individual settings per world
- **Multi-language** — Turkish (`tr`) and English (`en`) message files

## Commands

| Command | Description |
|---|---|
| `/rtp` | Opens solo teleport menu |
| `/rtp <world>` | Direct teleport to specified world |
| `/rtpqueue` | Opens 1v1 matchmaking menu |
| `/rtpqueue <world>` | Join queue for specified world |
| `/rtpqueue leave` | Leave the queue |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `lexonrtp.use` | `true` | Use RTP commands |
| `lexonrtp.cooldown.bypass` | `op` | Bypass cooldown |

## Installation

1. Download `LexonRTP.jar`
2. Place it in your server's `plugins/` folder
3. Restart the server
4. Configure `plugins/LexonRTP/config.yml` to your needs

## Configuration

```yaml
settings:
  language: en
  cooldown-seconds: 60
  rtp-countdown: 3

redis:
  enabled: false         # false = local, true = Redis (required for cross-server)

queue:
  enabled: true
  required-players: 2
  match-countdown: 10
  match-spacing: 10

worlds:
  overworld:
    world-name: "world"
    enabled: true
    min-radius: 500
    max-radius: 10000
    center-x: 0
    center-z: 0
    target-server: ""     # set to server name for cross-server RTP (e.g. "survival")
```

## Requirements

- Java 21
- Paper 1.20.4+ or Folia (uses the region-based scheduler API)

## Links

- GitHub: https://github.com/toprakdevx/LexonRTP
