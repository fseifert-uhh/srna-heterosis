CREATE TABLE IF NOT EXISTS cluster_data (
  cluster_id int(10) unsigned NOT NULL,
  chromosome int(10) unsigned NOT NULL,
  strand varchar(1) NOT NULL,
  start_position int(10) unsigned NOT NULL,
  end_position int(10) unsigned NOT NULL,
  sequence_id_text longtext NOT NULL,
  KEY cluster_id (cluster_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

CREATE TABLE IF NOT EXISTS `cluster_srna_expression` (
  `cluster_id` int(10) unsigned NOT NULL,
  `f037` double unsigned NOT NULL,
  `f039` double unsigned NOT NULL,
  `f043` double unsigned NOT NULL,
  `f047` double unsigned NOT NULL,
  `l024` double unsigned NOT NULL,
  `l035` double unsigned NOT NULL,
  `l043` double unsigned NOT NULL,
  `p033` double unsigned NOT NULL,
  `p040` double unsigned NOT NULL,
  `p046` double unsigned NOT NULL,
  `p048` double unsigned NOT NULL,
  `p063` double unsigned NOT NULL,
  `p066` double unsigned NOT NULL,
  `s028` double unsigned NOT NULL,
  `s036` double unsigned NOT NULL,
  `s044` double unsigned NOT NULL,
  `s046` double unsigned NOT NULL,
  `s049` double unsigned NOT NULL,
  `s050` double unsigned NOT NULL,
  `s058` double unsigned NOT NULL,
  `s067` double unsigned NOT NULL,
  `b73` double unsigned NOT NULL,
  `p033xf047` double unsigned NOT NULL,
  `s028xf039` double unsigned NOT NULL,
  `s028xl024` double unsigned NOT NULL,
  KEY `cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

CREATE TABLE IF NOT EXISTS `cluster_srna_expression_repeat_norm` (
  `cluster_id` int(10) unsigned NOT NULL,
  `f037` double unsigned NOT NULL,
  `f039` double unsigned NOT NULL,
  `f043` double unsigned NOT NULL,
  `f047` double unsigned NOT NULL,
  `l024` double unsigned NOT NULL,
  `l035` double unsigned NOT NULL,
  `l043` double unsigned NOT NULL,
  `p033` double unsigned NOT NULL,
  `p040` double unsigned NOT NULL,
  `p046` double unsigned NOT NULL,
  `p048` double unsigned NOT NULL,
  `p063` double unsigned NOT NULL,
  `p066` double unsigned NOT NULL,
  `s028` double unsigned NOT NULL,
  `s036` double unsigned NOT NULL,
  `s044` double unsigned NOT NULL,
  `s046` double unsigned NOT NULL,
  `s049` double unsigned NOT NULL,
  `s050` double unsigned NOT NULL,
  `s058` double unsigned NOT NULL,
  `s067` double unsigned NOT NULL,
  `b73` double unsigned NOT NULL,
  `p033xf047` double unsigned NOT NULL,
  `s028xf039` double unsigned NOT NULL,
  `s028xl024` double unsigned NOT NULL,
  KEY `cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

CREATE TABLE IF NOT EXISTS `cluster_srna_expression_distinct` (
  `cluster_id` int(10) unsigned NOT NULL,
  `f037` double unsigned NOT NULL,
  `f039` double unsigned NOT NULL,
  `f043` double unsigned NOT NULL,
  `f047` double unsigned NOT NULL,
  `l024` double unsigned NOT NULL,
  `l035` double unsigned NOT NULL,
  `l043` double unsigned NOT NULL,
  `p033` double unsigned NOT NULL,
  `p040` double unsigned NOT NULL,
  `p046` double unsigned NOT NULL,
  `p048` double unsigned NOT NULL,
  `p063` double unsigned NOT NULL,
  `p066` double unsigned NOT NULL,
  `s028` double unsigned NOT NULL,
  `s036` double unsigned NOT NULL,
  `s044` double unsigned NOT NULL,
  `s046` double unsigned NOT NULL,
  `s049` double unsigned NOT NULL,
  `s050` double unsigned NOT NULL,
  `s058` double unsigned NOT NULL,
  `s067` double unsigned NOT NULL,
  `b73` double unsigned NOT NULL,
  `p033xf047` double unsigned NOT NULL,
  `s028xf039` double unsigned NOT NULL,
  `s028xl024` double unsigned NOT NULL,
  KEY `cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;

CREATE TABLE IF NOT EXISTS `cluster_srna_expression_distinct_repeat_norm` (
  `cluster_id` int(10) unsigned NOT NULL,
  `f037` double unsigned NOT NULL,
  `f039` double unsigned NOT NULL,
  `f043` double unsigned NOT NULL,
  `f047` double unsigned NOT NULL,
  `l024` double unsigned NOT NULL,
  `l035` double unsigned NOT NULL,
  `l043` double unsigned NOT NULL,
  `p033` double unsigned NOT NULL,
  `p040` double unsigned NOT NULL,
  `p046` double unsigned NOT NULL,
  `p048` double unsigned NOT NULL,
  `p063` double unsigned NOT NULL,
  `p066` double unsigned NOT NULL,
  `s028` double unsigned NOT NULL,
  `s036` double unsigned NOT NULL,
  `s044` double unsigned NOT NULL,
  `s046` double unsigned NOT NULL,
  `s049` double unsigned NOT NULL,
  `s050` double unsigned NOT NULL,
  `s058` double unsigned NOT NULL,
  `s067` double unsigned NOT NULL,
  `b73` double unsigned NOT NULL,
  `p033xf047` double unsigned NOT NULL,
  `s028xf039` double unsigned NOT NULL,
  `s028xl024` double unsigned NOT NULL,
  KEY `cluster_id` (`cluster_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;
