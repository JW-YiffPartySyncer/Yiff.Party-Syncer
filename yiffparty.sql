SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

CREATE DATABASE IF NOT EXISTS `yiffparty` DEFAULT CHARACTER SET utf32 COLLATE utf32_general_ci;
USE `yiffparty`;

CREATE TABLE `categories` (
  `ID` int(11) NOT NULL,
  `Name` tinytext NOT NULL,
  `Path` tinytext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf32;

CREATE TABLE `patreons` (
  `ID` int(11) NOT NULL,
  `link` text NOT NULL,
  `name` text NOT NULL DEFAULT '',
  `last_checked` bigint(20) NOT NULL DEFAULT 0,
  `success` tinyint(1) NOT NULL DEFAULT 0,
  `wanted` tinyint(4) DEFAULT 0,
  `category` int(11) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf32;

CREATE TABLE `posts` (
  `ID` int(11) NOT NULL,
  `patreon` int(11) NOT NULL,
  `name` text NOT NULL,
  `href` text NOT NULL,
  `downloaded` tinyint(1) NOT NULL DEFAULT 0,
  `post` tinytext NOT NULL,
  `date` tinytext NOT NULL DEFAULT '',
  `last_checked` bigint(20) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf32;

CREATE TABLE `webrip` (
  `ID` int(11) NOT NULL,
  `Timestamp` int(11) NOT NULL,
  `Part` smallint(6) NOT NULL,
  `data` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf32;


ALTER TABLE `categories`
  ADD PRIMARY KEY (`ID`);

ALTER TABLE `patreons`
  ADD PRIMARY KEY (`ID`);

ALTER TABLE `posts`
  ADD PRIMARY KEY (`ID`),
  ADD KEY `HASH` (`href`(768)) USING HASH,
  ADD KEY `downloaded_ID_lastChecked` (`downloaded`,`ID`,`last_checked`);

ALTER TABLE `webrip`
  ADD PRIMARY KEY (`ID`);


ALTER TABLE `categories`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `patreons`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `posts`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE `webrip`
  MODIFY `ID` int(11) NOT NULL AUTO_INCREMENT;
COMMIT;

INSERT INTO `categories` (`name`, `path`) VALUES ("Uncategorized", "Uncategorized\\"); 

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
