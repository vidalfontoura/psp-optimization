sudo nohup sh ibea-daemon\
	350,400,1000 \
	60000,70000,100000 \
	SinglePointCrossover,MultiPointsCrossover\
	BitFlipMutation,LoopMoveOperator,LocalMoveOperator,SegmentMutation,OppositeMoveOperator \
	200 \
	0.9 \
	0.01,0.1,0.2 \
	30 \
	8 \
	results-chapter-backtrack-noperators\
	PPHHHPHHHHHHHHPPPHHHHHHHHHHPHPPPHHHHHHHHHHHHPPPPHHHHHHPHHPHP\
	20,50 &
