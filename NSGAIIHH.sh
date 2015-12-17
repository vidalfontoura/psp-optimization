java -jar target/NSGAIIHH.jar \
				300,400,500,600,700,900,1000 \
				60000 \
				SinglePointCrossover,IntegerTwoPointsCrossover,MultiPointsCrossover \
				BitFlipMutation,LoopMoveOperator,LocalMoveOperator,SegmentMutation,OppositeMoveOperator \
				1.0 \
				1.0 \
				30 \
				15 \
				result-hh \
				0 \
				0 \
				Random \
				HHHHPPPPHHHHHHHHHHHHPPPPPPHHHHHHHHHHHHPPPHHHHHHHHHHHHPPPHHHHHHHHHHHHPPPHPPHHPPHHPPHPH \
				false \
				20,50 &
