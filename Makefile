build: clean
	./scripts/generate-javadoc.sh
	./scripts/generate-shortnames.sh
	./scripts/default-to-latest.sh

clean:
	rm -rf build
