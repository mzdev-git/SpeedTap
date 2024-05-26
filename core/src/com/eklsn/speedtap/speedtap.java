package com.eklsn.speedtap;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class speedtap extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture clickerimg;
	BitmapFont font;
	Texture vfximg;
	List<Sprite> clickers;
	List<Sprite> vfx;
	OrthographicCamera camera;
	float clickersize = 192;
	float crsize = 64;
	final int SPAWN_DELTA = 150;
	final int FINISH_DELTA = 60000;
	long lastSpawn = 0;
	Music music;
	Sound sfx;
	Sound wrong;
	long now;
	int score = 0;
	long launchtime;
	public speedtap() {
		clickers = new ArrayList<>();
		vfx = new ArrayList<>();
	}

	@Override
	public void create() {
		batch = new SpriteBatch();

		clickerimg = new Texture("clicker.png");
		vfximg = new Texture("tapvfx.png");

		for (int i = 0; i < clickers.size(); i++) {
			clickers = new ArrayList<>();
		}
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1560, 720);
		Gdx.input.setInputProcessor((InputProcessor) this);
		music = Gdx.audio.newMusic(Gdx.files.internal("lvl1.ogg"));
		music.setLooping(true);
		music.play();
		sfx = Gdx.audio.newSound(Gdx.files.internal("snd.ogg"));
		wrong = Gdx.audio.newSound(Gdx.files.internal("wrong.ogg"));
		launchtime = TimeUtils.millis();
		font = new BitmapFont(Gdx.files.internal("catorze60.fnt"));
	}

	@Override
	public void render() {
		update();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		for (int i = 0; i < clickers.size(); i++) {
			clickers.get(i).draw(batch);
		}
		for (int i = 0; i < vfx.size(); i++) {
			vfx.get(i).draw(batch);
		}
		font.draw(batch, String.valueOf(score), 100,100);
		font.draw(batch, String.valueOf((FINISH_DELTA - (now - launchtime))/1000), 1410,100);
		batch.end();
	}

	@Override
	public void dispose() {
		batch.dispose();
		clickerimg.dispose();
		vfximg.dispose();
	}

	private void update() {
		now = TimeUtils.millis();
		for (int x = 0; x < clickers.size(); x++) {
			Sprite clicker = clickers.get(x);

			if (clicker.getColor().a < 0.99f && clicker.getRotation() == 0) {
				if (clicker.getColor().a < 0.9f) {
					clicker.setAlpha(clicker.getColor().a += 0.025F);
				}
				if (clicker.getColor().a > 0.9f) {
					clicker.setAlpha(clicker.getColor().a += 0.0005F);
				}
			}
			if (clicker.getRotation() == 1) {
				clicker.setAlpha(clicker.getColor().a -= 0.075F);
				if (clicker.getColor().a < 0.1f) {
					clicker.setScale(0);
				}
			}
			if (clicker.getRotation() == 2 && clicker.getScaleX() > 0) {
				clicker.setScale((float) ((float) clicker.getScaleX() - 0.1), (float) ((float) clicker.getScaleX() - 0.1));
			}
			if (clicker.getColor().a > 0.95f) {
				clicker.setRotation(1);
			}
		}
		for (int x = 0; x < vfx.size(); x++) {
			Sprite vfxspr = vfx.get(x);
			if (vfxspr.getScaleX() < 70 && vfxspr.getRotation() == 0) {
				vfxspr.setScale((int) vfxspr.getScaleX() + 2, (int) vfxspr.getScaleX() + 2);
			}
			if (vfxspr.getScaleX() > 68) {
				vfxspr.setScale(0);
				vfxspr.rotate(1);
			}
		}
		if (now - lastSpawn > SPAWN_DELTA) {
			Sprite clicker = new Sprite(clickerimg);
			crsize = Math.round(MathUtils.random(128, clickersize));
			clicker.setBounds(MathUtils.random(0, Gdx.graphics.getWidth() - crsize), MathUtils.random(0, Gdx.graphics.getHeight() - crsize), crsize, crsize);
			clicker.setOriginCenter();
			clicker.setAlpha(0);
			boolean green = MathUtils.randomBoolean();
			if (green) {
				clicker.setColor(0,1,0,0);
			}
			else { clicker.setColor(1,0,0,0);}
			clickers.add(clicker);
			lastSpawn = now;
		}
		if (now - launchtime > FINISH_DELTA) {
			Gdx.app.exit();
			System.exit(-1);
		}
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		for (int i = 0; i < 5; i++) {
			Vector3 position = camera.unproject(new Vector3(screenX, screenY, 0));
			for (int x = 0; x < clickers.size(); x++) {
				Sprite clicker = clickers.get(x);
				long now = TimeUtils.millis();

				if (clicker.getBoundingRectangle().contains(position.x, position.y)) {

					Sprite vfxspr = new Sprite(vfximg);
					vfxspr.setBounds(position.x, position.y, 50, 50);
					vfxspr.setOriginCenter();
					vfx.add(vfxspr);
					vfxspr.setColor(clicker.getColor());
					if(clicker.getRotation() != 2) {
						if (clicker.getColor().g == 1 && clicker.getColor().r == 0) {
							sfx.play(1.0f);
							score++;
						} else {
							wrong.play(1.0f);
							score-=10;
						}
					}
					clicker.setRotation(2);
					vfxspr.setAlpha(1);
					return true;
				}
			}
		}

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
}