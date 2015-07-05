#!/bin/bash
#-Dstats.block=10000000 \

# export PATH=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin:$PATH

nice \
	java \
		-Djava.awt.headless=true \
		-Dremote.sync=http://eternity.mkgi.net/ \
		-cp target/classes\
:$HOME/.m2/repository/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar\
:$HOME/.m2/repository/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar\
:$HOME/.m2/repository/commons-codec/commons-codec/1.2/commons-codec-1.2.jar\
		org.alcibiade.eternity.editor.application.ConsoleApp \
			EternityII.txt \
				"0xIterative Path MkI[90]" \
				"0xIterative Path MkIII[90]" \
				"0xSwap Weighted MkIV" \
				"0xSwap Weighted MkV" \
				"0xSwap Weighted MkV[1800]" \
				"0xSwap Weighted MkVI" \
				"0xSwap Weighted MkVII" \
				"8xPipeline[600]"
