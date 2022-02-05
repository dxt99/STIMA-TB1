## Greedy Bot 1.0 Logic
### Priority Order:
- Boost State Logic (sedang boosting, avoid every obs, use lizard if necessary) 1
- Fix Logic (damage >=3 langsung fix) 1
- Obstacle Logic (priority wall only, kalo depan kosong lanjut aja) 3
- Boost (speed 0/3/6/8, pastikan 15 tile didepan no obstacle, fix kalo perlu buat boost next round) 2
- EMP 2
- Boost (speed 9, pastikan 15 tile didepan no obstacle, fix kalo perlu buat boost next round) 2
- Obstacle Logic (all obs, simpan lizard kalo bisa) 3
- Tweet if speed 8/9/15? (di posisi musuh + speed + 1?) 1
- Oil Logic (musuh dibelakang and speed 8/9 atau max speed) 3
- Accelerate anyways

### Obstacle Logic (in general): 
- Avoid Wall/Mud/Oil = keluarin list of possible outcomes, wall priority
- Get powerup where available = choose best possible from above
- Lizard = kalo list diatas kosong, just use lizard
- else do nothing
