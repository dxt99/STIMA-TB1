## Greedy Bot 1.0 Logic
### Priority Order:
- Boost State Logic (sedang boosting, avoid every obs) 1
- Fix Logic (damage >=3 langsung fix) 1
- Obstacle Logic (priority wall only) 3
- Tweet (taro di depan musuh + kita ga bakal nabrak) 1
- Boost (musuh speed 0/3/6) 2
- EMP 2
- Boost (musuh speed 8/9) 2
- Obstacle Logic (all obs) 3
- Oil Logic (musuh dibelakang atau max speed) 3
- Accelerate anyways

### Obstacle Logic (in general): 
- Lizard kalo ada obstacle = keluarin command lizard
- Avoid Wall/Mud/Oil = keluarin list of possible outcomes
- Get powerup where available = choose best possible from above
- else do nothing
