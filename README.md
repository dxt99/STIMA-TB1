## Greedy Bot v1 Logic (currently in starter)
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

## Greedy Bot v2 Changlog (currently in reference)
### quality of life:
	- moved a lot of stuff to functions

### bot logic:
	- added do nothing and decelerate conditions (pretty damn useful)
	- improved map reading (projected accel path, projected decel path, projected boost path)
	- improved cybertruck logic (makes it impossible for self-crashing)
	- improved mud tanking logic

### known bugs:
	- emp spamming when desperate
	- turning into opponent

### possible improvements:
	- block opponent movement (turn into their lane)
	- improve emp logic (when opponent is boosting, etc.)
	- use boost for selfish gains when desperate (boost to max speed when damage>0)
